package com.bryan.repository;

import com.bryan.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    List<InventoryBatch> findByProductIdAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(Long productId, java.math.BigDecimal quantity);

    boolean existsByBatchCode(String batchCode);

    boolean existsByBatchCodeAndIdNot(String batchCode, Long id);
}

