package com.bryan.dto.request;

import com.bryan.entity.ShipmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShipmentRequest(
    @NotNull(message = "Order ID is required")
    Long orderId,

    @NotNull(message = "Provider ID is required")
    Long providerId,

    @Size(max = 100, message = "Tracking code không được vượt quá 100 ký tự")
    String trackingCode,

    @NotNull(message = "Shipping fee is required")
    @DecimalMin(value = "0.0", message = "Shipping fee must be >= 0")
    BigDecimal shippingFee
) {}
