package com.bryan.repository;

import com.bryan.entity.InventoryBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    List<InventoryBatch> findByProductIdAndQuantityRemainingGreaterThanOrderByExpiryDateAsc(Long productId, java.math.BigDecimal quantity);

    Optional<InventoryBatch> findFirstByProductIdOrderByExpiryDateAsc(Long productId);

    boolean existsByBatchCode(String batchCode);

    boolean existsByBatchCodeAndIdNot(String batchCode, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b FROM InventoryBatch b
            WHERE b.product.id = :productId
              AND b.quantityRemaining > 0
              AND b.expiryDate > :today
            ORDER BY b.expiryDate ASC
            """)
    List<InventoryBatch> findAvailableByProductIdForUpdate(@Param("productId") Long productId,
                                                           @Param("today") LocalDate today);

    @Query("""
            SELECT COALESCE(SUM(b.quantityRemaining), 0) FROM InventoryBatch b
            WHERE b.product.id = :productId
              AND b.quantityRemaining > 0
              AND b.expiryDate > :today
            """)
    java.math.BigDecimal sumAvailableQuantity(@Param("productId") Long productId,
                                              @Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(p.id) FROM Product p
            WHERE p.isActive = true
              AND COALESCE((SELECT SUM(b.quantityRemaining) FROM InventoryBatch b WHERE b.product = p), 0) <= :threshold
            """)
    long countLowStockProducts(@Param("threshold") java.math.BigDecimal threshold);
}
