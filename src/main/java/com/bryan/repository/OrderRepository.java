package com.bryan.repository;

import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"details", "details.product", "address", "promotion", "statusHistories"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"details", "details.product", "address", "promotion", "statusHistories"})
    Optional<Order> findByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {"details", "details.product", "address", "promotion", "statusHistories"})
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"details", "details.product", "address", "promotion", "statusHistories"})
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findAllOrders(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    boolean existsByOrderCode(String orderCode);

    @Query("SELECT COUNT(p) > 0 FROM PaymentRequest p WHERE p.id = :paymentRequestId AND p.order IS NOT NULL")
    boolean existsByPaymentRequestId(@Param("paymentRequestId") Long paymentRequestId);

    @Query("SELECT o FROM Order o WHERE o.deliveryDate = :date ORDER BY o.createdAt DESC")
    Page<Order> findByDeliveryDate(@Param("date") java.time.LocalDate date, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.buildingCodeSnapshot = :buildingCode ORDER BY o.createdAt DESC")
    Page<Order> findByBuildingCode(@Param("buildingCode") String buildingCode, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.deliverySlotId = :slotId ORDER BY o.createdAt DESC")
    Page<Order> findByDeliverySlotId(@Param("slotId") Long slotId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.deliveryMethod = :method ORDER BY o.createdAt DESC")
    Page<Order> findByDeliveryMethod(@Param("method") com.bryan.entity.DeliveryMethod method, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliverySlotId = :slotId AND o.deliveryDate = :date")
    long countByDeliverySlotIdAndDeliveryDate(@Param("slotId") Long slotId, @Param("date") java.time.LocalDate date);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(OrderStatus status);
}
