package com.bryan.service;

import com.bryan.entity.ProductCategory;
import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getAllCategories();
    ProductCategory createCategory(ProductCategory category);
}

