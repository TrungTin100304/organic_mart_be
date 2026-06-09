package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private UserAddress address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "order_code", nullable = false, unique = true, length = 20)
    private String orderCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Check(constraints = "status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'DELIVERING', 'DELIVERED', 'CANCELLED', 'REFUNDED')")
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "shipping_recipient_snapshot", nullable = false, length = 100)
    private String shippingRecipientSnapshot;

    @Column(name = "shipping_phone_snapshot", nullable = false, length = 20)
    private String shippingPhoneSnapshot;

    @Column(name = "shipping_address_snapshot", nullable = false, columnDefinition = "TEXT")
    private String shippingAddressSnapshot;

    @Column(name = "shipping_provider_name_snapshot", length = 100)
    private String shippingProviderNameSnapshot;

    // Internal delivery fields
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 20)
    private DeliveryMethod deliveryMethod;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_slot_id")
    private Long deliverySlotId;

    @Column(name = "delivery_slot_snapshot", length = 100)
    private String deliverySlotSnapshot;

    @Column(name = "building_code_snapshot", length = 20)
    private String buildingCodeSnapshot;

    @Column(name = "building_name_snapshot", length = 200)
    private String buildingNameSnapshot;

    @Column(name = "floor_snapshot", length = 20)
    private String floorSnapshot;

    @Column(name = "apartment_number_snapshot", length = 20)
    private String apartmentNumberSnapshot;

    @Column(name = "recipient_name_snapshot", length = 100)
    private String recipientNameSnapshot;

    @Column(name = "recipient_phone_snapshot", length = 20)
    private String recipientPhoneSnapshot;

    @Column(name = "delivery_note_snapshot", columnDefinition = "TEXT")
    private String deliveryNoteSnapshot;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderDetail> details = new HashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addDetail(OrderDetail detail) {
        details.add(detail);
        detail.setOrder(this);
    }

    public void addStatusHistory(OrderStatusHistory history) {
        statusHistories.add(history);
        history.setOrder(this);
    }
}
