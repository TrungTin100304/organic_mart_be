package com.bryan.service;

import com.bryan.dto.request.MealPlanGenerationRequest;
import com.bryan.dto.request.MealUpdateRequest;
import com.bryan.dto.response.MealPlanResponse;
import com.bryan.dto.response.MealResponse;
import com.bryan.dto.response.ShoppingListItemResponse;

import java.util.List;

public interface MealPlanService {
    MealPlanResponse generateMealPlan(Long userId, MealPlanGenerationRequest request);
    List<MealPlanResponse> getMealPlans(Long userId);
    MealPlanResponse getMealPlanById(Long id, Long userId);
    void deleteMealPlan(Long id, Long userId);
    MealResponse updateMeal(Long mealPlanId, Long mealId, Long userId, MealUpdateRequest request);
    MealResponse regenerateMeal(Long mealPlanId, Long mealId, Long userId);
    List<ShoppingListItemResponse> getShoppingList(Long mealPlanId, Long userId);
    Object addToCart(Long mealPlanId, Long userId);
}
