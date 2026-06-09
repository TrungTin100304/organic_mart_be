package com.bryan.service;

import com.bryan.entity.Order;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.Promotion;
import com.bryan.entity.User;

import java.math.BigDecimal;

public interface PromotionRedemptionService {

    AppliedPromotion reserve(String code, User user, BigDecimal subtotal);

    void recordUsage(AppliedPromotion applied, User user, Order order, PaymentRequest paymentRequest);

    void attachOrder(PaymentRequest paymentRequest, Order order);

    void releaseReservation(PaymentRequest paymentRequest);

    record AppliedPromotion(Promotion promotion, BigDecimal discountAmount) {
    }
}
