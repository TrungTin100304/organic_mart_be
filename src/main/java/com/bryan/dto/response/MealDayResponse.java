package com.bryan.dto.response;

import com.bryan.entity.MealType;

import java.util.List;

public record MealDayResponse(
        int dayNumber,
        List<MealResponse> meals,
        int totalCalories,
        int totalProtein,
        int totalCarbs,
        int totalFat
) {}
