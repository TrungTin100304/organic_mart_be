package com.bryan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DeliverySettingRequest(
        @NotNull @DecimalMin("0") BigDecimal fee,
        @DecimalMin("0") BigDecimal freeShippingThreshold,
        @Min(0) Integer estimatedMinutes,
        @Min(0) Integer displayOrder,
        boolean enabled
) {
}
