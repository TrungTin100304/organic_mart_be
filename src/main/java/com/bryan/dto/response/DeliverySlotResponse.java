package com.bryan.dto.response;

import com.bryan.entity.DeliverySlot;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record DeliverySlotResponse(
    Long id,
    String name,
    LocalTime startTime,
    LocalTime endTime,
    Integer cutoffMinutes,
    Integer maximumOrders,
    Integer displayOrder,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DeliverySlotResponse from(DeliverySlot entity) {
        return new DeliverySlotResponse(
            entity.getId(),
            entity.getName(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getCutoffMinutes(),
            entity.getMaximumOrders(),
            entity.getDisplayOrder(),
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
