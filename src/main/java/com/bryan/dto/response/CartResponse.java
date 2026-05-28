package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
    Long id,
    Long userId,
    BigDecimal totalQuantity,
    BigDecimal totalPrice,
    int distinctItemCount,
    LocalDateTime updatedAt,
    List<CartItemResponse> items
) {}
