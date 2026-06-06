package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VietQrWebhookRequest(
    @NotBlank String transferCode,
    @NotNull @Positive BigDecimal amount,
    @NotBlank String transactionId
) {}
