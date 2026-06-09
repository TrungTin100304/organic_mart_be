package com.bryan.dto.response;

import com.bryan.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {
    private Long id;
    private MealType mealType;
    private String name;
    private String description;
    private List<String> ingredients;
    private String cookingInstructions;
    private Integer preparationMinutes;
    private Integer cookingMinutes;
    private int calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private List<MealProductResponse> products;
}
