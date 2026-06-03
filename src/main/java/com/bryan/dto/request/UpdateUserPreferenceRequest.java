package com.bryan.dto.request;

import com.bryan.entity.DietType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateUserPreferenceRequest(
    @Positive(message = "Chiều cao phải lớn hơn 0")
    @DecimalMin(value = "0.01", message = "Chiều cao phải lớn hơn 0")
    BigDecimal heightCm,

    @Positive(message = "Cân nặng phải lớn hơn 0")
    @DecimalMin(value = "0.01", message = "Cân nặng phải lớn hơn 0")
    BigDecimal weightKg,

    @Size(max = 100, message = "Mục tiêu sức khỏe không được vượt quá 100 ký tự")
    String healthGoal,

    DietType dietType,

    @PositiveOrZero(message = "Lượng calo hàng ngày phải lớn hơn hoặc bằng 0")
    Integer dailyCalorieTarget
) {}
