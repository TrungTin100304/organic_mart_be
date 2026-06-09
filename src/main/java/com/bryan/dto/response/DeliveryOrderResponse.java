package com.bryan.dto.response;

import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import com.bryan.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DeliveryOrderResponse(
    Long id,
    String orderCode,
    Long userId,
    String userFullName,
    OrderStatus status,
    PaymentMethod paymentMethod,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
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
    String deliveryNoteSnapshot,
    String note
) {
    public static DeliveryOrderResponse from(Order order) {
        return new DeliveryOrderResponse(
            order.getId(),
            order.getOrderCode(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getUser() != null ? order.getUser().getFullName() : null,
            order.getStatus(),
            order.getPaymentMethod(),
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getShippingFee(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getDeliveryMethod(),
            order.getDeliveryDate(),
            order.getDeliverySlotId(),
            order.getDeliverySlotSnapshot(),
            order.getBuildingCodeSnapshot(),
            order.getBuildingNameSnapshot(),
            order.getFloorSnapshot(),
            order.getApartmentNumberSnapshot(),
            order.getRecipientNameSnapshot(),
            order.getRecipientPhoneSnapshot(),
            order.getDeliveryNoteSnapshot(),
            order.getNote()
        );
    }
}
