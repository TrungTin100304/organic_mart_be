package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record ProductRequest(
    @NotBlank(message = "Product name is required")
    String name,

    @NotNull(message = "Category ID is required")
    Long categoryId,

    String description,

    String storageInstructions,

    String detailedDescription,

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    BigDecimal price,

    String unit,

    Map<String, Object> nutritionPer100g,

    MultipartFile imageFile,

    boolean isActive,

    Set<Long> allergenIds
) {}
