package com.bryan.dto.response;

import com.bryan.entity.OrderStatus;
import com.bryan.entity.OrderStatusHistory;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(
    Long id,
    OrderStatus fromStatus,
    OrderStatus toStatus,
    Long changedById,
    String changedByName,
    String note,
    LocalDateTime createdAt
) {
    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
            history.getId(),
            history.getFromStatus(),
            history.getToStatus(),
            history.getChangedBy() != null ? history.getChangedBy().getId() : null,
            history.getChangedBy() != null ? history.getChangedBy().getFullName() : null,
            history.getNote(),
            history.getCreatedAt()
        );
    }
}
