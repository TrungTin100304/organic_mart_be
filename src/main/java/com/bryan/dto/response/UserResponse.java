package com.bryan.dto.response;

import com.bryan.entity.Role;
import com.bryan.entity.DietType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String phoneNumber,
    String email,
    String avatarUrl,
    boolean isActive,
    Role role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    // Preference fields
    BigDecimal heightCm,
    BigDecimal weightKg,
    BigDecimal bmi,
    String healthGoal,
    DietType dietType,
    Integer dailyCalorieTarget
) {}

