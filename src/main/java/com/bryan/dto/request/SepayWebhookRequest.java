package com.bryan.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SePay real webhook payload.
 * All fields use camelCase as sent by SePay.
 *
 * @param id               SePay's unique transaction ID (used for idempotency)
 * @param gateway         Payment gateway identifier
 * @param transactionDate Date/time of the transaction
 * @param accountNumber   Target bank account number (String to preserve leading zeros)
 * @param subAccount      Sub-account if any
 * @param code            Transfer code (OM... prefix) — may be null; fallback to content field
 * @param content         Transaction description/content (may contain embedded code)
 * @param transferType    "in" = credit, "out" = debit
 * @param description     Human-readable description
 * @param transferAmount  Amount in VND
 * @param accumulated     Accumulated balance
 * @param referenceCode   Reference / order code
 */
public record SepayWebhookRequest(
    @NotNull
    Long id,

    String gateway,

    LocalDateTime transactionDate,

    @JsonProperty("accountNumber")
    String accountNumber,

    @JsonProperty("subAccount")
    String subAccount,

    String code,

    String content,

    @JsonProperty("transferType")
    String transferType,

    String description,

    @JsonProperty("transferAmount")
    BigDecimal transferAmount,

    BigDecimal accumulated,

    @JsonProperty("referenceCode")
    String referenceCode
) {}
