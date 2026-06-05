package com.bryan.dto.response;

import com.bryan.entity.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentResponse(
    Long id,
    Long orderId,
    String orderCode,
    ShippingProviderResponse provider,
    String trackingCode,
    BigDecimal shippingFee,
    ShipmentStatus currentStatus,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt,
    java.util.List<ShipmentTrackingResponse> trackings,
    LocalDateTime createdAt
) {}
