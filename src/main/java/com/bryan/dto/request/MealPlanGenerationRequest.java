package com.bryan.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record MealPlanGenerationRequest(
        @NotNull(message = "Số ngày là bắt buộc")
        @Min(value = 1, message = "Số ngày tối thiểu là 1")
        @Max(value = 7, message = "Số ngày tối đa là 7")
        Integer numberOfDays,

        @NotNull(message = "Số bữa ăn là bắt buộc")
        @Min(value = 1, message = "Số bữa tối thiểu là 1")
        @Max(value = 4, message = "Số bữa tối đa là 4")
        Integer mealsPerDay,

        @NotNull(message = "Số khẩu phần là bắt buộc")
        @Min(value = 1, message = "Số khẩu phần tối thiểu là 1")
        @Max(value = 10, message = "Số khẩu phần tối đa là 10")
        Integer servings,

        @NotBlank(message = "Chế độ ăn là bắt buộc")
        String dietType,

        @Min(value = 500, message = "Mục tiêu calo tối thiểu là 500")
        @Max(value = 5000, message = "Mục tiêu calo tối đa là 5000")
        Integer dailyCalorieTarget,

        @DecimalMin(value = "0.0", message = "Ngân sách không được âm")
        BigDecimal budgetMax,

        @Min(value = 5, message = "Thời gian nấu tối thiểu là 5 phút")
        @Max(value = 240, message = "Thời gian nấu tối đa là 240 phút")
        Integer maxCookingMinutes,

        List<String> preferredIngredients,

        List<String> excludedIngredients,

        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String additionalNotes
) {}
