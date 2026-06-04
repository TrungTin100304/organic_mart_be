package com.bryan.service.impl;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.entity.ProductCategory;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategory getCategoryById(Long id) {
        return findCategoryById(id);
    }

    @Override
    public ProductCategory createCategory(ProductCategoryRequest request) {
        ProductCategory category = new ProductCategory();
        applyRequest(category, request);
        return categoryRepository.save(category);
    }

    @Override
    public ProductCategory updateCategory(Long id, ProductCategoryRequest request) {
        ProductCategory category = findCategoryById(id);
        applyRequest(category, request);
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category with existing products");
        }
        categoryRepository.deleteById(id);
    }

    private ProductCategory findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void applyRequest(ProductCategory category, ProductCategoryRequest request) {
        category.setName(request.name());
        category.setSlug(slugify.slugify(request.name()));
        category.setSortOrder(request.sortOrder());
        category.setParent(resolveParent(category.getId(), request.parentId()));
    }

    private ProductCategory resolveParent(Long categoryId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new BadRequestException("Category cannot be its own parent");
        }
        return findCategoryById(parentId);
    }
}

