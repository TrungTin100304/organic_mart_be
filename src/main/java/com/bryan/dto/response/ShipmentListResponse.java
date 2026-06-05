package com.bryan.dto.response;

import com.bryan.entity.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentListResponse(
    Long id,
    Long orderId,
    String orderCode,
    String providerName,
    String trackingCode,
    BigDecimal shippingFee,
    ShipmentStatus currentStatus,
    LocalDateTime createdAt
) {}
