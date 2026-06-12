package com.bryan.dto.request;

import java.util.List;

public class AiMeal {
    public String mealType;
    public String name;
    public String description;
    public List<String> ingredients;
    public String cookingInstructions;
    public Integer preparationMinutes;
    public Integer cookingMinutes;
    public int calories;
    public double proteinGrams;
    public double carbsGrams;
    public double fatGrams;

    public AiMeal() {}
}
