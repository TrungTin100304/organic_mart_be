package com.bryan.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeliverySlotRequest(
    @NotBlank(message = "Slot name is required")
    @Size(max = 100, message = "Slot name must not exceed 100 characters")
    String name,

    @NotNull(message = "Start time is required")
    String startTime,

    @NotNull(message = "End time is required")
    String endTime,

    @Min(value = 0, message = "Cutoff minutes must be non-negative")
    Integer cutoffMinutes,

    @Min(value = 1, message = "Maximum orders must be at least 1")
    Integer maximumOrders,

    @Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder
) {}
