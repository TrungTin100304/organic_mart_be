package com.bryan.dto.response;

import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.OrderStatus;
import com.bryan.entity.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    String shippingProviderNameSnapshot,
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
    java.time.LocalDateTime updatedAt,
    // Internal delivery
    DeliveryMethod deliveryMethod,
    LocalDate deliveryDate,
    Long deliverySlotId,
    String deliverySlotSnapshot,
    String buildingCodeSnapshot,
    String buildingNameSnapshot,
    String floorSnapshot,
    String apartmentNumberSnapshot,
    String recipientNameSnapshot,
    String recipientPhoneSnapshot,
    String deliveryNoteSnapshot
) {
    public record PromotionSnapshotResponse(
        Long id,
        String code,
        PromotionType type,
        BigDecimal value
    ) {}
}
