package com.bryan.service.impl;

import com.bryan.config.InternalDeliveryProperties;
import com.bryan.service.DeliverySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliverySettingServiceImpl implements DeliverySettingService {

    private final InternalDeliveryProperties deliveryProperties;

    @Override
    public List<Setting> getAllSettings() {
        return List.of(
                new Setting(
                        "STANDARD",
                        deliveryProperties.getStandardFee(),
                        BigDecimal.ZERO,
                        deliveryProperties.getStandardMinutes(),
                        1,
                        true),
                new Setting(
                        "EXPRESS",
                        deliveryProperties.getExpressFee(),
                        BigDecimal.ZERO,
                        deliveryProperties.getExpressMinutes(),
                        2,
                        true),
                new Setting(
                        "SCHEDULED",
                        deliveryProperties.getScheduledFee(),
                        BigDecimal.ZERO,
                        null,
                        3,
                        true)
        );
    }
}
