package com.bryan.dto.request;

import com.bryan.entity.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminPromotionRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull PromotionType type,
        @NotNull @Positive BigDecimal value,
        @PositiveOrZero BigDecimal minOrderAmount,
        @PositiveOrZero BigDecimal maxDiscountAmount,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTo,
        @Positive Integer usageLimit,
        @Positive Integer usageLimitPerUser,
        boolean active
) {}
