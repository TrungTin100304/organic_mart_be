package com.bryan.service.impl;

import com.bryan.entity.InventoryBatch;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAllocationService {

    private final InventoryBatchRepository batchRepository;

    @Transactional
    public List<Allocation> allocate(Long productId, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product quantity must be greater than zero");
        }

        List<InventoryBatch> batches =
                batchRepository.findAvailableByProductIdForUpdate(productId, LocalDate.now());
        BigDecimal available = batches.stream()
                .map(InventoryBatch::getQuantityRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(requestedQuantity) < 0) {
            throw new BadRequestException("Insufficient stock for product id: " + productId);
        }

        BigDecimal remaining = requestedQuantity;
        List<Allocation> allocations = new ArrayList<>();
        List<InventoryBatch> changedBatches = new ArrayList<>();
        for (InventoryBatch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
            BigDecimal allocated = batch.getQuantityRemaining().min(remaining);
            batch.setQuantityRemaining(batch.getQuantityRemaining().subtract(allocated));
            changedBatches.add(batch);
            allocations.add(new Allocation(batch, allocated));
            remaining = remaining.subtract(allocated);
        }

        batchRepository.saveAll(changedBatches);
        return List.copyOf(allocations);
    }

    public boolean hasAvailableStock(Long productId, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal available = batchRepository.sumAvailableQuantity(productId, LocalDate.now());
        return available != null && available.compareTo(requestedQuantity) >= 0;
    }

    public record Allocation(InventoryBatch batch, BigDecimal quantity) {
    }
}
