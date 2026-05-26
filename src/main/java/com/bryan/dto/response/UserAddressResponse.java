package com.bryan.dto.response;

import java.time.LocalDateTime;

import com.bryan.entity.AddressLabel;

public record UserAddressResponse(
    Long id,
    AddressLabel label,
    String customLabel,
    String recipientName,
    String recipientPhone,
    String fullAddress,
    String ward,
    String district,
    String city,
    boolean isDefault,
    LocalDateTime createdAt
) {}
