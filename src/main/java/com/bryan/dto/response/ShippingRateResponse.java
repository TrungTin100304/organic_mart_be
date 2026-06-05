package com.bryan.dto.response;

import java.math.BigDecimal;

public record ShippingRateResponse(
    Long providerId,
    String providerName,
    BigDecimal fee,
    String estimatedDays
) {}
