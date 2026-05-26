package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.bryan.entity.AddressLabel;

public record UserAddressRequest(
    @NotNull(message = "Label is required")
    AddressLabel label,

    @Size(max = 100, message = "Custom label must not exceed 100 characters")
    String customLabel,

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    String recipientName,

    @NotBlank(message = "Recipient phone is required")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    String recipientPhone,

    @NotBlank(message = "Full address is required")
    String fullAddress,

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    String ward,

    @Size(max = 100, message = "District must not exceed 100 characters")
    String district,

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,

    boolean isDefault
) {}
