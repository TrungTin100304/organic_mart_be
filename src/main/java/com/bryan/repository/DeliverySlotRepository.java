package com.bryan.repository;

import com.bryan.entity.DeliverySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliverySlotRepository extends JpaRepository<DeliverySlot, Long> {
    List<DeliverySlot> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT ds FROM DeliverySlot ds WHERE ds.isActive = true ORDER BY ds.displayOrder ASC")
    List<DeliverySlot> findActiveSlotsOrdered();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliverySlotId = :slotId AND o.deliveryDate = :date")
    long countOrdersForSlotOnDate(@Param("slotId") Long slotId, @Param("date") LocalDate date);
}
