package com.bryan.repository;

import com.bryan.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findAllActive(@Param("active") boolean active);

    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.inventoryBatches
        WHERE p.isActive = true
        """)
    List<Product> findAllActiveWithInventory();
}
