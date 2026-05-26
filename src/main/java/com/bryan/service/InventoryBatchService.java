package com.bryan.service;

import com.bryan.dto.request.InventoryBatchRequest;
import com.bryan.dto.response.InventoryBatchResponse;
import com.bryan.dto.response.ProductTraceabilityResponse;

import java.util.List;

public interface InventoryBatchService {
    List<InventoryBatchResponse> getAllBatches();

    InventoryBatchResponse getBatchById(Long id);

    InventoryBatchResponse createBatch(InventoryBatchRequest request);

    InventoryBatchResponse updateBatch(Long id, InventoryBatchRequest request);

    void deleteBatch(Long id);

    List<InventoryBatchResponse> getBatchesByProductId(Long productId);

    ProductTraceabilityResponse getProductTraceability(Long productId);
}

