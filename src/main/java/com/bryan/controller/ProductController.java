package com.bryan.controller;

import com.bryan.dto.request.ProductRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ProductResponse;
import com.bryan.dto.response.ProductTraceabilityResponse;
import com.bryan.entity.Product;
import com.bryan.mapper.ProductMapper;
import com.bryan.service.ProductService;
import com.bryan.service.InventoryBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final InventoryBatchService inventoryBatchService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getAllProducts(pageable);
        Page<ProductResponse> responsePage = productPage.map(productMapper::toResponse);
        return ApiResponse.success(responsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ApiResponse.success(productMapper.toResponse(product));
    }

    @GetMapping("/{id}/traceability")
    public ResponseEntity<ApiResponse<ProductTraceabilityResponse>> getProductTraceability(@PathVariable Long id) {
        return ApiResponse.success(inventoryBatchService.getProductTraceability(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @ModelAttribute ProductRequest request) {
        Product createdProduct = productService.createProduct(request);
        return ApiResponse.success(201, productMapper.toResponse(createdProduct));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @ModelAttribute ProductRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ApiResponse.success(productMapper.toResponse(updatedProduct));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null, "Product deleted successfully");
    }
}
