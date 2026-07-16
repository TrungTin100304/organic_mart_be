package com.bryan.dto.response;

import com.bryan.entity.AuditLog;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long performedById,
        String performedByEmail,
        String details,
        LocalDateTime createdAt
) {
    public static AdminAuditLogResponse from(AuditLog log) {
        return new AdminAuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getPerformedBy() != null ? log.getPerformedBy().getId() : null,
                log.getPerformedBy() != null ? log.getPerformedBy().getEmail() : null,
                log.getDetails(),
                log.getCreatedAt());
    }
}
