package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record ProductResponse(
    Long id,
    String name,
    String slug,
    String description,
    String storageInstructions,
    String detailedDescription,
    BigDecimal price,
    String unit,
    Map<String, Object> nutritionPer100g,
    String imageUrl,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    ProductCategoryResponse category,
    Set<AllergenResponse> allergens
) {}

