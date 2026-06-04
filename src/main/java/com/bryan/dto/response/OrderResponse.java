package com.bryan.dto.response;

import com.bryan.entity.OrderStatus;
import com.bryan.entity.PromotionType;

import java.math.BigDecimal;

public record OrderResponse(
    Long id,
    String orderCode,
    Long userId,
    String userFullName,
    Long addressId,
    String addressLabel,
    String shippingRecipientSnapshot,
    String shippingPhoneSnapshot,
    String shippingAddressSnapshot,
    PromotionSnapshotResponse promotion,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    OrderStatus status,
    String note,
    java.util.List<OrderDetailResponse> details,
    java.util.List<OrderStatusHistoryResponse> statusHistories,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public record PromotionSnapshotResponse(
        Long id,
        String code,
        PromotionType type,
        java.math.BigDecimal value
    ) {}
}
