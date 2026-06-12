package com.bryan.dto.request;

import com.bryan.entity.AddressLabel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserAddressRequest(
    @NotNull(message = "Label is required")
    AddressLabel label,

    @Size(max = 100)
    String customLabel,

    @NotNull(message = "Recipient name is required")
    @Size(max = 100)
    String recipientName,

    @NotNull(message = "Recipient phone is required")
    @Size(max = 20)
    String recipientPhone,

    String fullAddress,

    @Size(max = 100)
    String ward,

    @Size(max = 100)
    String district,

    @Size(max = 100)
    String city,

    boolean isDefault,

    Long buildingId,

    @Size(max = 20)
    String floor,

    @Size(max = 20)
    String apartmentNumber,

    String deliveryNote
) {}
