package com.bryan.dto.response;

import com.bryan.entity.MealPlanStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MealPlanResponse(
        Long id,
        String name,
        String startDate,
        int numberOfDays,
        int mealsPerDay,
        int servings,
        String dietType,
        Integer dailyCalorieTarget,
        java.math.BigDecimal budgetMax,
        Integer maxCookingMinutes,
        String additionalNotes,
        MealPlanStatus status,
        List<MealDayResponse> days,
        Integer totalCaloriesPerDay,
        Integer totalProteinPerDay,
        Integer totalCarbsPerDay,
        Integer totalFatPerDay,
        String errorMessage,
        LocalDateTime createdAt
) {}
