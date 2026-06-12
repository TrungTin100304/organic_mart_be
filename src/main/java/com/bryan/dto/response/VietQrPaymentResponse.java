package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VietQrPaymentResponse(
    Long id,
    BigDecimal amount,
    String status,
    String transferCode,
    String qrUrl,
    String bankId,
    String accountNo,
    String accountName,
    LocalDateTime expiresAt,
    LocalDateTime paidAt,
    Long orderId,
    String orderCode
) {}
