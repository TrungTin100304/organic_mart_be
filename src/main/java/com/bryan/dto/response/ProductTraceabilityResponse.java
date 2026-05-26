package com.bryan.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductTraceabilityResponse(
    Long productId,
    String productName,
    String productSlug,
    String categoryName,
    BigDecimal totalQuantityInitial,
    BigDecimal totalQuantityRemaining,
    List<InventoryBatchResponse> batches
) {}

