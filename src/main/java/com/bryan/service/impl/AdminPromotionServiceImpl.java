package com.bryan.service.impl;

import com.bryan.dto.request.AdminPromotionRequest;
import com.bryan.dto.response.AdminPromotionResponse;
import com.bryan.entity.Promotion;
import com.bryan.entity.PromotionType;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.PromotionRepository;
import com.bryan.service.AdminPromotionService;
import com.bryan.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private final PromotionRepository promotionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminPromotionResponse> getAll() {
        return promotionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminPromotionResponse::from)
                .toList();
    }

    @Override
    public AdminPromotionResponse create(AdminPromotionRequest request) {
        String code = normalizeCode(request.code());
        if (promotionRepository.existsByCode(code)) {
            throw new BadRequestException("Promotion code already exists: " + code);
        }
        Promotion promotion = new Promotion();
        promotion.setTimesUsed(0);
        apply(promotion, request, code);
        Promotion saved = promotionRepository.save(promotion);
        auditLogService.log("PROMOTION_CREATED", "PROMOTION", saved.getId(), "Created promotion " + saved.getCode());
        return AdminPromotionResponse.from(saved);
    }

    @Override
    public AdminPromotionResponse update(Long id, AdminPromotionRequest request) {
        Promotion promotion = find(id);
        String code = normalizeCode(request.code());
        if (promotionRepository.existsByCodeAndIdNot(code, id)) {
            throw new BadRequestException("Promotion code already exists: " + code);
        }
        apply(promotion, request, code);
        Promotion saved = promotionRepository.save(promotion);
        auditLogService.log("PROMOTION_UPDATED", "PROMOTION", saved.getId(), "Updated promotion " + saved.getCode());
        return AdminPromotionResponse.from(saved);
    }

    @Override
    public AdminPromotionResponse deactivate(Long id) {
        Promotion promotion = find(id);
        promotion.setActive(false);
        Promotion saved = promotionRepository.save(promotion);
        auditLogService.log("PROMOTION_DEACTIVATED", "PROMOTION", saved.getId(), "Deactivated promotion " + saved.getCode());
        return AdminPromotionResponse.from(saved);
    }

    private void apply(Promotion promotion, AdminPromotionRequest request, String code) {
        if (request.validTo().isBefore(request.validFrom())) {
            throw new BadRequestException("Promotion end date must not be before start date");
        }
        if (request.type() == PromotionType.PERCENTAGE
                && request.value().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Percentage promotion cannot exceed 100");
        }
        if (request.usageLimit() != null && request.usageLimitPerUser() != null
                && request.usageLimitPerUser() > request.usageLimit()) {
            throw new BadRequestException("Per-user limit cannot exceed total usage limit");
        }
        promotion.setCode(code);
        promotion.setName(request.name().trim());
        promotion.setDescription(request.description());
        promotion.setType(request.type());
        promotion.setValue(request.value());
        promotion.setMinOrderAmount(request.minOrderAmount());
        promotion.setMaxDiscountAmount(request.maxDiscountAmount());
        promotion.setValidFrom(request.validFrom());
        promotion.setValidTo(request.validTo());
        promotion.setUsageLimit(request.usageLimit());
        promotion.setUsageLimitPerUser(request.usageLimitPerUser());
        promotion.setActive(request.active());
    }

    private Promotion find(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
