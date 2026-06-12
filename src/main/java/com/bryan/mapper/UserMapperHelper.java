package com.bryan.mapper;

import com.bryan.entity.DietType;
import com.bryan.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UserMapperHelper {

    public static BigDecimal getHeight(User user) {
        if (user.getUserPreference() != null && user.getUserPreference().getHeightCm() != null) {
            return user.getUserPreference().getHeightCm();
        }
        return null;
    }

    public static BigDecimal getWeight(User user) {
        if (user.getUserPreference() != null && user.getUserPreference().getWeightKg() != null) {
            return user.getUserPreference().getWeightKg();
        }
        return null;
    }

    public static BigDecimal calculateBmi(User user) {
        if (user.getUserPreference() == null) return null;
        var p = user.getUserPreference();
        if (p.getHeightCm() == null || p.getWeightKg() == null) return null;
        if (p.getHeightCm().compareTo(BigDecimal.ZERO) <= 0) return null;
        BigDecimal heightM = p.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return p.getWeightKg().divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
    }

    public static String getHealthGoal(User user) {
        if (user.getUserPreference() != null) return user.getUserPreference().getHealthGoal();
        return null;
    }

    public static DietType getDietType(User user) {
        if (user.getUserPreference() != null) return user.getUserPreference().getDietType();
        return null;
    }

    public static Integer getDailyCalorieTarget(User user) {
        if (user.getUserPreference() != null) return user.getUserPreference().getDailyCalorieTarget();
        return null;
    }
}
