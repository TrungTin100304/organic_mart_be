package com.bryan.controller;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ProductCategoryResponse;
import com.bryan.entity.ProductCategory;
import com.bryan.mapper.ProductCategoryMapper;
import com.bryan.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;
    private final ProductCategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getAllCategories() {
        List<ProductCategoryResponse> responses = categoryService.getAllCategories().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ApiResponse.success(categoryMapper.toResponse(categoryService.getCategoryById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createCategory(@Valid @RequestBody ProductCategoryRequest request) {
        ProductCategory createdCategory = categoryService.createCategory(request);
        return ApiResponse.success(201, categoryMapper.toResponse(createdCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> updateCategory(@PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        ProductCategory updatedCategory = categoryService.updateCategory(id, request);
        return ApiResponse.success(categoryMapper.toResponse(updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null, "Category deleted successfully");
    }
}

