package com.bryan.dto.response;

import java.math.BigDecimal;

public record MealProductResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        String productImageUrl,
        String productUnit,
        String originalIngredientName,
        BigDecimal quantity,
        String unit,
        BigDecimal estimatedPrice,
        boolean isInStock,
        boolean addedToCart
) {}
