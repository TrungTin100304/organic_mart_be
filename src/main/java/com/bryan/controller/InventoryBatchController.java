package com.bryan.controller;

import com.bryan.dto.request.InventoryBatchRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.InventoryBatchResponse;
import com.bryan.dto.response.ProductTraceabilityResponse;
import com.bryan.service.InventoryBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-batches")
@RequiredArgsConstructor
public class InventoryBatchController {

    private final InventoryBatchService inventoryBatchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryBatchResponse>>> getAllBatches() {
        return ApiResponse.success(inventoryBatchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> getBatchById(@PathVariable Long id) {
        return ApiResponse.success(inventoryBatchService.getBatchById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryBatchResponse>>> getBatchesByProductId(@PathVariable Long productId) {
        return ApiResponse.success(inventoryBatchService.getBatchesByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> createBatch(@Valid @RequestBody InventoryBatchRequest request) {
        return ApiResponse.success(201, inventoryBatchService.createBatch(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> updateBatch(@PathVariable Long id, @Valid @RequestBody InventoryBatchRequest request) {
        return ApiResponse.success(inventoryBatchService.updateBatch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable Long id) {
        inventoryBatchService.deleteBatch(id);
        return ApiResponse.success(null, "Inventory batch deleted successfully");
    }

    @GetMapping("/product/{productId}/traceability")
    public ResponseEntity<ApiResponse<ProductTraceabilityResponse>> getProductTraceability(@PathVariable Long productId) {
        return ApiResponse.success(inventoryBatchService.getProductTraceability(productId));
    }
}
