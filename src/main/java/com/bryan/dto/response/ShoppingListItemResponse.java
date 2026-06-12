package com.bryan.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingListItemResponse(
        String key,
        String originalIngredientName,
        BigDecimal totalQuantity,
        String unit,
        List<MealProductResponse> products,
        boolean isFullyMapped,
        boolean isAnyInStock,
        BigDecimal totalEstimatedPrice
) {}
