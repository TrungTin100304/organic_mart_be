package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record ProductRequest(
    @NotBlank(message = "Tên sản phẩm là bắt buộc")
    String name,

    @NotNull(message = "ID danh mục là bắt buộc")
    Long categoryId,

    String description,

    String storageInstructions,

    String detailedDescription,

    @NotNull(message = "Giá là bắt buộc")
    @PositiveOrZero(message = "Giá phải lớn hơn hoặc bằng 0")
    BigDecimal price,

    String unit,

    Map<String, Object> nutritionPer100g,

    MultipartFile imageFile,

    boolean isActive,

    Set<Long> allergenIds
) {}
