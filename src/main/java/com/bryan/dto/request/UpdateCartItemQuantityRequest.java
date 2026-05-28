package com.bryan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateCartItemQuantityRequest(
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    BigDecimal quantity
) {}
