package com.bryan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CalculateShippingRateRequest(
    @NotNull(message = "Provider ID is required")
    Long providerId,

    @NotNull(message = "Province is required")
    String province,

    @NotNull(message = "District is required")
    String district,

    String ward,

    @NotNull(message = "Weight is required")
    @PositiveOrZero(message = "Weight must be >= 0")
    BigDecimal weightKg
) {}
