package com.bryan.service.impl;

import com.bryan.config.InternalDeliveryProperties;
import com.bryan.dto.request.DeliverySettingRequest;
import com.bryan.entity.DeliveryMethod;
import com.bryan.exception.BadRequestException;
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

    @Override
    public Setting updateSetting(DeliveryMethod deliveryMethod, DeliverySettingRequest request) {
        if (deliveryMethod == null) {
            throw new BadRequestException("Delivery method is required");
        }
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        switch (deliveryMethod) {
            case STANDARD -> {
                deliveryProperties.setStandardFee(request.fee());
                if (request.estimatedMinutes() != null) {
                    deliveryProperties.setStandardMinutes(request.estimatedMinutes());
                }
                return new Setting(
                        "STANDARD",
                        deliveryProperties.getStandardFee(),
                        request.freeShippingThreshold() != null ? request.freeShippingThreshold() : BigDecimal.ZERO,
                        deliveryProperties.getStandardMinutes(),
                        request.displayOrder() != null ? request.displayOrder() : 1,
                        request.enabled());
            }
            case EXPRESS -> {
                deliveryProperties.setExpressFee(request.fee());
                if (request.estimatedMinutes() != null) {
                    deliveryProperties.setExpressMinutes(request.estimatedMinutes());
                }
                return new Setting(
                        "EXPRESS",
                        deliveryProperties.getExpressFee(),
                        request.freeShippingThreshold() != null ? request.freeShippingThreshold() : BigDecimal.ZERO,
                        deliveryProperties.getExpressMinutes(),
                        request.displayOrder() != null ? request.displayOrder() : 2,
                        request.enabled());
            }
            case SCHEDULED -> {
                deliveryProperties.setScheduledFee(request.fee());
                return new Setting(
                        "SCHEDULED",
                        deliveryProperties.getScheduledFee(),
                        request.freeShippingThreshold() != null ? request.freeShippingThreshold() : BigDecimal.ZERO,
                        request.estimatedMinutes(),
                        request.displayOrder() != null ? request.displayOrder() : 3,
                        request.enabled());
            }
            default -> throw new BadRequestException("Unsupported delivery method: " + deliveryMethod);
        }
    }
}
