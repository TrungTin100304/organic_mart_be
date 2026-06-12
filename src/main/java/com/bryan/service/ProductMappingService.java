package com.bryan.service;

import com.bryan.dto.response.MealProductResponse;
import com.bryan.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductMappingService {
    Optional<MealProductResponse> findMatchingProduct(String ingredientName);
    List<MealProductResponse> mapIngredients(List<String> ingredientNames);
}
