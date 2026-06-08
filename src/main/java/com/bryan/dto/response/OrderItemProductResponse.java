package com.bryan.dto.response;

import com.bryan.entity.Allergen;
import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record OrderItemProductResponse(
    Long id,
    String name,
    String slug,
    String description,
    String storageInstructions,
    String unit,
    Map<String, Object> nutritionPer100g,
    String imageUrl,
    ProductCategoryResponse category,
    Set<AllergenInfo> allergens
) {
    public record AllergenInfo(
        Long id,
        String name
    ) {
        public static AllergenInfo from(Allergen allergen) {
            return new AllergenInfo(allergen.getId(), allergen.getName());
        }
    }

    public static OrderItemProductResponse from(Product product) {
        if (product == null) {
            return null;
        }

        ProductCategoryResponse catResponse = null;
        if (product.getCategory() != null) {
            ProductCategory cat = product.getCategory();
            catResponse = new ProductCategoryResponse(
                cat.getId(),
                cat.getName(),
                cat.getSlug(),
                cat.getParent() != null ? cat.getParent().getId() : null,
                cat.getSortOrder(),
                cat.getCreatedAt()
            );
        }

        Set<AllergenInfo> allergenInfos = null;
        if (product.getAllergens() != null && !product.getAllergens().isEmpty()) {
            allergenInfos = product.getAllergens().stream()
                .map(AllergenInfo::from)
                .collect(Collectors.toSet());
        }

        return new OrderItemProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getDescription(),
            product.getStorageInstructions(),
            product.getUnit(),
            product.getNutritionPer100g(),
            product.getImageUrl(),
            catResponse,
            allergenInfos
        );
    }
}
