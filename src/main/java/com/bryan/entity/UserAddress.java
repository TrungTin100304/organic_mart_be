package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_address")
@Getter
@Setter
@NoArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AddressLabel label;

    @Column(name = "custom_label", length = 100)
    private String customLabel;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    @Column(length = 100)
    private String ward;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    public boolean getIsDefault() { return this.isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }

    // Internal delivery fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private ResidentialBuilding building;

    @Column(length = 20)
    private String floor;

    @Column(name = "apartment_number", length = 20)
    private String apartmentNumber;

    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
