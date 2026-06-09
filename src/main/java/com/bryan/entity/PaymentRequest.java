package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_request")
@Getter
@Setter
@NoArgsConstructor
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private UserAddress address;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transfer_code", nullable = false, unique = true, length = 50)
    private String transferCode;

    @Column(name = "qr_url", nullable = false, columnDefinition = "TEXT")
    private String qrUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "payment_items_snapshot", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String paymentItemsSnapshot;

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
}
