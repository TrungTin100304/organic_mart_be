package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryBatchRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Farm ID is required")
    Long farmId,

    @NotBlank(message = "Batch code is required")
    String batchCode,

    @NotNull(message = "Quantity initial is required")
    @Positive(message = "Quantity initial must be greater than 0")
    BigDecimal quantityInitial,

    @NotNull(message = "Quantity remaining is required")
    @PositiveOrZero(message = "Quantity remaining must be greater than or equal to 0")
    BigDecimal quantityRemaining,

    @NotNull(message = "Import date is required")
    LocalDate importDate,

    @NotNull(message = "Expiry date is required")
    LocalDate expiryDate,

    BigDecimal costPrice
) {}
