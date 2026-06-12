package com.bryan.repository;

import com.bryan.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    List<InventoryBatch> findByProductIdAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(Long productId, java.math.BigDecimal quantity);

    Optional<InventoryBatch> findFirstByProductIdOrderByExpiryDateAsc(Long productId);

    boolean existsByBatchCode(String batchCode);

    boolean existsByBatchCodeAndIdNot(String batchCode, Long id);

    @Query("""
            SELECT COUNT(p.id) FROM Product p
            WHERE p.isActive = true
              AND COALESCE((SELECT SUM(b.quantityRemaining) FROM InventoryBatch b WHERE b.product = p), 0) <= :threshold
            """)
    long countLowStockProducts(@Param("threshold") java.math.BigDecimal threshold);
}
