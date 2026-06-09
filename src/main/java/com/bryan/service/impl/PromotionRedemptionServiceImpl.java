package com.bryan.service.impl;

import com.bryan.entity.Order;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.Promotion;
import com.bryan.entity.PromotionType;
import com.bryan.entity.PromotionUsage;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.PromotionRepository;
import com.bryan.repository.PromotionUsageRepository;
import com.bryan.service.PromotionRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionRedemptionServiceImpl implements PromotionRedemptionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    @Override
    public AppliedPromotion reserve(String code, User user, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return null;
        }

        Promotion promotion = promotionRepository.findByCodeForUpdate(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new BadRequestException("Invalid promotion code"));
        LocalDate today = LocalDate.now();
        if (!promotion.isActive() || today.isBefore(promotion.getValidFrom()) || today.isAfter(promotion.getValidTo())) {
            throw new BadRequestException("Invalid or expired promotion code");
        }
        if (promotion.getUsageLimit() != null && promotion.getTimesUsed() >= promotion.getUsageLimit()) {
            throw new BadRequestException("This promotion has reached its usage limit");
        }
        if (promotion.getUsageLimitPerUser() != null
                && promotionUsageRepository.countByPromotionIdAndUserId(promotion.getId(), user.getId())
                >= promotion.getUsageLimitPerUser()) {
            throw new BadRequestException("You have already used this promotion the maximum number of times");
        }
        if (promotion.getMinOrderAmount() != null && subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Order amount does not meet minimum requirement for this promotion");
        }

        promotion.setTimesUsed(promotion.getTimesUsed() + 1);
        promotionRepository.save(promotion);
        return new AppliedPromotion(promotion, calculateDiscount(promotion, subtotal));
    }

    @Override
    public void recordUsage(AppliedPromotion applied, User user, Order order, PaymentRequest paymentRequest) {
        if (applied == null) {
            return;
        }
        PromotionUsage usage = new PromotionUsage();
        usage.setPromotion(applied.promotion());
        usage.setUser(user);
        usage.setOrder(order);
        usage.setPaymentRequest(paymentRequest);
        promotionUsageRepository.save(usage);
    }

    @Override
    public void attachOrder(PaymentRequest paymentRequest, Order order) {
        promotionUsageRepository.findByPaymentRequestId(paymentRequest.getId()).ifPresent(usage -> {
            usage.setOrder(order);
            promotionUsageRepository.save(usage);
        });
    }

    @Override
    public void releaseReservation(PaymentRequest paymentRequest) {
        promotionUsageRepository.findByPaymentRequestId(paymentRequest.getId()).ifPresent(usage -> {
            Promotion promotion = promotionRepository.findByIdForUpdate(usage.getPromotion().getId())
                    .orElse(usage.getPromotion());
            promotion.setTimesUsed(Math.max(0, promotion.getTimesUsed() - 1));
            promotionRepository.save(promotion);
            promotionUsageRepository.delete(usage);
        });
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal discount = promotion.getType() == PromotionType.PERCENTAGE
                ? subtotal.multiply(promotion.getValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : promotion.getValue();
        if (promotion.getMaxDiscountAmount() != null
                && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }
        return discount.min(subtotal);
    }
}
