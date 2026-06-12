package com.bryan.dto.request;

import com.bryan.entity.DeliveryMethod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VietQrPaymentRequest(
    @NotNull(message = "addressId is required")
    Long addressId,

    @NotNull(message = "deliveryMethod is required")
    DeliveryMethod deliveryMethod,

    LocalDate deliveryDate,

    Long deliverySlotId
) {}
