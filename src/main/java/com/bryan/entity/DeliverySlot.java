package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "delivery_slot")
@Getter
@Setter
@NoArgsConstructor
public class DeliverySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "cutoff_minutes", nullable = false)
    private Integer cutoffMinutes = 0;

    @Column(name = "maximum_orders", nullable = false)
    private Integer maximumOrders = 999;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DeliverySlot(String name, LocalTime startTime, LocalTime endTime, Integer cutoffMinutes, Integer maximumOrders, Integer displayOrder) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cutoffMinutes = cutoffMinutes != null ? cutoffMinutes : 0;
        this.maximumOrders = maximumOrders != null ? maximumOrders : 999;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = true;
    }
}
