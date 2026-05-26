package com.bryan.service.impl;

import com.bryan.entity.ProductCategory;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.service.ProductCategoryService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public ProductCategory createCategory(ProductCategory category) {
        category.setSlug(slugify.slugify(category.getName()));
        return categoryRepository.save(category);
    }
}

