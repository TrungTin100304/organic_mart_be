package com.bryan.dto.response;

import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentRequestResponse(
        Long id,
        Long userId,
        String userName,
        String transferCode,
        String transactionId,
        PaymentStatus status,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal amount,
        Long orderId,
        String orderCode,
        DeliveryMethod deliveryMethod,
        String buildingCode,
        String apartmentNumber,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime paidAt
) {
    public static AdminPaymentRequestResponse from(PaymentRequest payment) {
        return new AdminPaymentRequestResponse(
                payment.getId(),
                payment.getUser() != null ? payment.getUser().getId() : null,
                payment.getUser() != null ? payment.getUser().getFullName() : null,
                payment.getTransferCode(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getSubtotal(),
                payment.getShippingFee(),
                payment.getAmount(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getOrder() != null ? payment.getOrder().getOrderCode() : null,
                payment.getDeliveryMethod(),
                payment.getBuildingCodeSnapshot(),
                payment.getApartmentNumberSnapshot(),
                payment.getCreatedAt(),
                payment.getExpiresAt(),
                payment.getPaidAt()
        );
    }
}
