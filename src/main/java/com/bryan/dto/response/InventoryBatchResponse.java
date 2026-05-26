package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InventoryBatchResponse(
    Long id,
    Long productId,
    String productName,
    Long farmId,
    String farmName,
    String batchCode,
    BigDecimal quantityInitial,
    BigDecimal quantityRemaining,
    LocalDate importDate,
    LocalDate expiryDate,
    BigDecimal costPrice,
    boolean expired,
    LocalDateTime createdAt
) {}

