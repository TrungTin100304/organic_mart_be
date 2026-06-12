package com.bryan.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AiMealRequest(
        @NotNull @Min(1) Integer numberOfDays,
        @NotNull @Min(1) Integer mealsPerDay,
        @NotNull @Min(1) Integer servings,
        @NotNull String dietType,
        @Min(500) Integer dailyCalorieTarget,
        Double budgetMax,
        @Min(5) Integer maxCookingMinutes,
        List<String> preferredIngredients,
        List<String> excludedIngredients,
        String additionalNotes,
        List<String> userAllergenNames
) {}
