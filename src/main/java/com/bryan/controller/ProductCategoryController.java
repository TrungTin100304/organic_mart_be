package com.bryan.controller;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ProductCategoryResponse;
import com.bryan.entity.ProductCategory;
import com.bryan.mapper.ProductCategoryMapper;
import com.bryan.service.ProductCategoryService;
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

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createCategory(@RequestBody ProductCategoryRequest request) {
        ProductCategory category = categoryMapper.toEntity(request);
        ProductCategory createdCategory = categoryService.createCategory(category);
        return ApiResponse.success(201, categoryMapper.toResponse(createdCategory));
    }
}

