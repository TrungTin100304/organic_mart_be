package com.bryan.dto.response;

import com.bryan.entity.SepayWebhookEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSepayWebhookEventResponse(
        Long id,
        String sepayTransactionId,
        String referenceCode,
        String transferCode,
        BigDecimal transferAmount,
        String transferType,
        String gateway,
        SepayWebhookEvent.EventStatus status,
        String rejectionReason,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
    public static AdminSepayWebhookEventResponse from(SepayWebhookEvent event) {
        return new AdminSepayWebhookEventResponse(
                event.getId(),
                event.getSepayTransactionId(),
                event.getReferenceCode(),
                event.getTransferCode(),
                event.getTransferAmount(),
                event.getTransferType(),
                event.getGateway(),
                event.getStatus(),
                event.getRejectionReason(),
                event.getProcessedAt(),
                event.getCreatedAt()
        );
    }
}
