package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductCategoryRequest(
    @NotBlank(message = "Tên danh mục là bắt buộc")
    String name,

    Long parentId,

    @PositiveOrZero(message = "Thứ tự sắp xếp phải là số dương hoặc bằng 0")
    int sortOrder
) {}

