package com.bryan.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record MealUpdateRequest(
        @NotBlank(message = "Tên món ăn là bắt buộc")
        @Size(max = 200, message = "Tên món ăn không được vượt quá 200 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
        String description,

        List<String> ingredients,

        @Size(max = 5000, message = "Hướng dẫn nấu không được vượt quá 5000 ký tự")
        String cookingInstructions,

        @Min(value = 0, message = "Thời gian chuẩn bị không được âm")
        Integer preparationMinutes,

        @Min(value = 0, message = "Thời gian nấu không được âm")
        Integer cookingMinutes,

        @Min(value = 0, message = "Calo không được âm")
        Integer calories,

        @Min(value = 0, message = "Protein không được âm")
        java.math.BigDecimal proteinGrams,

        @Min(value = 0, message = "Carbs không được âm")
        java.math.BigDecimal carbsGrams,

        @Min(value = 0, message = "Chất béo không được âm")
        java.math.BigDecimal fatGrams
) {}
