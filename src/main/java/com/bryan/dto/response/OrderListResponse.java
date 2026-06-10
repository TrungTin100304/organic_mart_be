package com.bryan.dto.response;

import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import com.bryan.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderListResponse(
    Long id,
    String orderCode,
    Long userId,
    String userFullName,
    OrderStatus status,
    PaymentMethod paymentMethod,
    BigDecimal totalAmount,
    int itemCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OrderListResponse from(Order order) {
        return new OrderListResponse(
            order.getId(),
            order.getOrderCode(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getUser() != null ? order.getUser().getFullName() : null,
            order.getStatus(),
            order.getPaymentMethod(),
            order.getTotalAmount(),
            order.getDetails() != null ? order.getDetails().size() : 0,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
