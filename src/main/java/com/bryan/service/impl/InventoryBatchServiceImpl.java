package com.bryan.service.impl;

import com.bryan.dto.request.InventoryBatchRequest;
import com.bryan.dto.response.InventoryBatchResponse;
import com.bryan.dto.response.ProductTraceabilityResponse;
import com.bryan.entity.Farm;
import com.bryan.entity.InventoryBatch;
import com.bryan.entity.Product;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.InventoryBatchMapper;
import com.bryan.repository.FarmRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.service.InventoryBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryBatchServiceImpl implements InventoryBatchService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final InventoryBatchMapper inventoryBatchMapper;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchResponse> getAllBatches() {
        return inventoryBatchRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(inventoryBatchMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBatchResponse getBatchById(Long id) {
        return inventoryBatchMapper.toResponse(getBatchEntityById(id));
    }

    @Override
    public InventoryBatchResponse createBatch(InventoryBatchRequest request) {
        validateRequest(request, null);

        InventoryBatch inventoryBatch = inventoryBatchMapper.toEntity(request);
        inventoryBatch.setProduct(getProductById(request.productId()));
        inventoryBatch.setFarm(getFarmById(request.farmId()));

        InventoryBatch savedBatch = inventoryBatchRepository.save(inventoryBatch);
        return inventoryBatchMapper.toResponse(savedBatch);
    }

    @Override
    public InventoryBatchResponse updateBatch(Long id, InventoryBatchRequest request) {
        InventoryBatch existingBatch = getBatchEntityById(id);
        validateRequest(request, id);

        inventoryBatchMapper.updateEntity(request, existingBatch);
        existingBatch.setProduct(getProductById(request.productId()));
        existingBatch.setFarm(getFarmById(request.farmId()));

        return inventoryBatchMapper.toResponse(existingBatch);
    }

    @Override
    public void deleteBatch(Long id) {
        if (!inventoryBatchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory batch not found with id: " + id);
        }
        inventoryBatchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchResponse> getBatchesByProductId(Long productId) {
        getProductById(productId);
        return inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId)
            .stream()
            .map(inventoryBatchMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductTraceabilityResponse getProductTraceability(Long productId) {
        Product product = getProductById(productId);
        List<InventoryBatchResponse> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId)
            .stream()
            .map(inventoryBatchMapper::toResponse)
            .toList();

        BigDecimal totalInitial = batches.stream()
            .map(InventoryBatchResponse::quantityInitial)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = batches.stream()
            .map(InventoryBatchResponse::quantityRemaining)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductTraceabilityResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getCategory().getName(),
            totalInitial,
            totalRemaining,
            batches
        );
    }

    private void validateRequest(InventoryBatchRequest request, Long currentBatchId) {
        if (request.expiryDate().isBefore(request.importDate()) || request.expiryDate().isEqual(request.importDate())) {
            throw new BadRequestException("Expiry date must be after import date");
        }

        if (request.quantityRemaining().compareTo(request.quantityInitial()) > 0) {
            throw new BadRequestException("Quantity remaining must be less than or equal to quantity initial");
        }

        boolean duplicateBatchCode = currentBatchId == null
            ? inventoryBatchRepository.existsByBatchCode(request.batchCode())
            : inventoryBatchRepository.existsByBatchCodeAndIdNot(request.batchCode(), currentBatchId);

        if (duplicateBatchCode) {
            throw new BadRequestException("Batch code already exists: " + request.batchCode());
        }
    }

    private InventoryBatch getBatchEntityById(Long id) {
        return inventoryBatchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory batch not found with id: " + id));
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Farm getFarmById(Long id) {
        return farmRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
    }
}

