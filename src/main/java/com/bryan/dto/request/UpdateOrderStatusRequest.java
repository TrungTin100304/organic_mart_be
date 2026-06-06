package com.bryan.dto.request;

import com.bryan.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required")
    OrderStatus status,

    String note
) {}
