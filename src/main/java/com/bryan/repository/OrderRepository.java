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
}
