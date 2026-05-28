package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponse(
    Long id,
    Long productId,
    String productName,
    String productSlug,
    String imageUrl,
    BigDecimal unitPrice,
    String unit,
    BigDecimal quantity,
    BigDecimal subtotal,
    LocalDateTime addedAt
) {}
