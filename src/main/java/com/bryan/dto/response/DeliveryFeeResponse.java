package com.bryan.dto.response;

import com.bryan.entity.DeliveryMethod;

import java.math.BigDecimal;

public record DeliveryFeeResponse(
    DeliveryMethod deliveryMethod,
    BigDecimal shippingFee,
    BigDecimal freeShippingThreshold,
    int estimatedMinutes,
    String estimatedTime
) {}
