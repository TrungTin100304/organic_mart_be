package com.bryan.service;

import java.math.BigDecimal;

public interface ShippingProviderApiService {

    String getProviderName();

    ShippingRateResponse calculateRate(ShippingRateRequest request);

    String createOrder(CreateShippingOrderRequest request);

    ShippingOrderInfo getOrderInfo(String trackingCode);

    record ShippingRateRequest(
        String province,
        String district,
        String ward,
        BigDecimal weightKg
    ) {}

    record ShippingRateResponse(
        BigDecimal fee,
        String estimatedDays,
        String providerName
    ) {}

    record CreateShippingOrderRequest(
        String orderCode,
        String senderName,
        String senderPhone,
        String senderAddress,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        BigDecimal weightKg
    ) {}

    record ShippingOrderInfo(
        String trackingCode,
        BigDecimal fee,
        String status
    ) {}
}
