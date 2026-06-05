package com.bryan.repository;

import com.bryan.entity.Shipment;
import com.bryan.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"order", "provider", "trackings"})
    Optional<Shipment> findById(Long id);

    @EntityGraph(attributePaths = {"order", "provider", "trackings"})
    Optional<Shipment> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order", "provider", "trackings"})
    Optional<Shipment> findByTrackingCode(String trackingCode);

    boolean existsByOrderId(Long orderId);

    @Query("SELECT s FROM Shipment s WHERE s.currentStatus = :status ORDER BY s.createdAt DESC")
    Page<Shipment> findByStatus(@Param("status") ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s ORDER BY s.createdAt DESC")
    Page<Shipment> findAllShipments(Pageable pageable);
}
