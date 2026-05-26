package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductCategoryRequest(
    @NotBlank(message = "Category name is required")
    String name,

    Long parentId,

    @PositiveOrZero(message = "Sort order must be a positive number or zero")
    int sortOrder
) {}

