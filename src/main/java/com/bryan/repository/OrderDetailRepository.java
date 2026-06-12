package com.bryan.repository;

import com.bryan.entity.OrderDetail;
import com.bryan.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderId(Long orderId);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM OrderDetail d
            WHERE d.order.id = :orderId
              AND d.order.user.id = :userId
              AND d.product.id = :productId
              AND d.order.status = com.bryan.entity.OrderStatus.DELIVERED
            """)
    boolean existsDeliveredPurchase(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("orderId") Long orderId);

    @Query("""
            SELECT d.product.name, SUM(d.quantity)
            FROM OrderDetail d
            WHERE d.order.status NOT IN :excluded
            GROUP BY d.product.id, d.product.name
            ORDER BY SUM(d.quantity) DESC
            """)
    List<Object[]> findTopProducts(@Param("excluded") Set<OrderStatus> excluded, Pageable pageable);

    @Query("""
            SELECT d.product.category.name, SUM(d.quantity * d.priceAtPurchase)
            FROM OrderDetail d
            WHERE d.order.status NOT IN :excluded
            GROUP BY d.product.category.id, d.product.category.name
            ORDER BY SUM(d.quantity * d.priceAtPurchase) DESC
            """)
    List<Object[]> findCategoryRevenue(@Param("excluded") Set<OrderStatus> excluded);
}
