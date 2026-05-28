package com.bryan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddCartItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    BigDecimal quantity
) {}
