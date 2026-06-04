package com.bryan.service;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.entity.ProductCategory;
import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getAllCategories();
    ProductCategory getCategoryById(Long id);
    ProductCategory createCategory(ProductCategoryRequest request);
    ProductCategory updateCategory(Long id, ProductCategoryRequest request);
    void deleteCategory(Long id);
}

