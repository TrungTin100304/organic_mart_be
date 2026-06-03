package com.bryan.dto.response;

import com.bryan.entity.DietType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserPreferenceResponse(
    Long id,
    Long userId,
    BigDecimal heightCm,
    BigDecimal weightKg,
    BigDecimal bmi,
    String healthGoal,
    DietType dietType,
    Integer dailyCalorieTarget,
    LocalDateTime updatedAt
) {}
