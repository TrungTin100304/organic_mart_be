package com.bryan.service.impl;

import com.bryan.service.ShippingProviderApiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class GhnApiService implements ShippingProviderApiService {

    private static final String PROVIDER_NAME = "GHN";
    private final Random random = new Random();

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ShippingRateResponse calculateRate(ShippingRateRequest request) {
        BigDecimal baseFee = new BigDecimal("15000");
        BigDecimal weightFee = request.weightKg().multiply(new BigDecimal("5000"));
        BigDecimal totalFee = baseFee.add(weightFee);

        return new ShippingRateResponse(
            totalFee,
            "2-3 ngày",
            PROVIDER_NAME
        );
    }

    @Override
    public String createOrder(CreateShippingOrderRequest request) {
        String code = "GHN" + System.currentTimeMillis();
        return code.substring(0, Math.min(code.length(), 15));
    }

    @Override
    public ShippingOrderInfo getOrderInfo(String trackingCode) {
        return new ShippingOrderInfo(
            trackingCode,
            new BigDecimal("25000"),
            "Delivering"
        );
    }
}
