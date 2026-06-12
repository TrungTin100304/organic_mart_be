package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sepay_webhook_event")
@Getter
@Setter
@NoArgsConstructor
public class SepayWebhookEvent {

    public enum EventStatus {
        RECEIVED,
        PROCESSED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sepay_transaction_id", nullable = false, unique = true, length = 100)
    private String sepayTransactionId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "transfer_code", length = 100)
    private String transferCode;

    @Column(name = "transfer_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal transferAmount;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "transfer_type", length = 20)
    private String transferType;

    @Column(length = 50)
    private String gateway;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.RECEIVED;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
