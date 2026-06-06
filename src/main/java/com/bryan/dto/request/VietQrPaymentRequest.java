package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VietQrPaymentRequest(
    @NotNull Long addressId,
    @NotBlank
    @Pattern(regexp = "STANDARD|EXPRESS", message = "shippingMethod must be STANDARD or EXPRESS")
    String shippingMethod
) {}
