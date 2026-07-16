package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_settings")
@Getter
@Setter
@NoArgsConstructor
public class DeliverySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, unique = true, length = 20)
    private DeliveryMethod deliveryType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "free_shipping_threshold", precision = 12, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private boolean enabled = true;
}
