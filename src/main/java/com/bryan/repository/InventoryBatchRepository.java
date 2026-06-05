package com.bryan.repository;

import com.bryan.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    List<InventoryBatch> findByProductIdAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(Long productId, java.math.BigDecimal quantity);

    Optional<InventoryBatch> findFirstByProductIdOrderByExpiryDateAsc(Long productId);

    boolean existsByBatchCode(String batchCode);

    boolean existsByBatchCodeAndIdNot(String batchCode, Long id);
}
