package com.bryan.service.impl;

import com.bryan.service.ShippingProviderApiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class GhtkApiService implements ShippingProviderApiService {

    private static final String PROVIDER_NAME = "GHTK";
    private final Random random = new Random();

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ShippingRateResponse calculateRate(ShippingRateRequest request) {
        BigDecimal baseFee = new BigDecimal("12000");
        BigDecimal weightFee = request.weightKg().multiply(new BigDecimal("4000"));
        BigDecimal totalFee = baseFee.add(weightFee);

        return new ShippingRateResponse(
            totalFee,
            "3-5 ngày",
            PROVIDER_NAME
        );
    }

    @Override
    public String createOrder(CreateShippingOrderRequest request) {
        String code = "GHTK" + System.currentTimeMillis();
        return code.substring(0, Math.min(code.length(), 15));
    }

    @Override
    public ShippingOrderInfo getOrderInfo(String trackingCode) {
        return new ShippingOrderInfo(
            trackingCode,
            new BigDecimal("22000"),
            "Delivering"
        );
    }
}
