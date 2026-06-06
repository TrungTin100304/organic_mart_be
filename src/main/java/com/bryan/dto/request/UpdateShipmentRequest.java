package com.bryan.dto.request;

import com.bryan.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShipmentRequest(
    @NotNull(message = "Provider ID is required")
    Long providerId,

    @Size(max = 100, message = "Tracking code không được vượt quá 100 ký tự")
    String trackingCode,

    @NotNull(message = "Shipping fee is required")
    java.math.BigDecimal shippingFee,

    @NotNull(message = "Status is required")
    ShipmentStatus currentStatus
) {}
