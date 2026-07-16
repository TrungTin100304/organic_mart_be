package com.bryan.service;

import com.bryan.dto.request.DeliverySettingRequest;
import com.bryan.entity.DeliveryMethod;

import java.util.List;

public interface DeliverySettingService {
    record Setting(String deliveryType, java.math.BigDecimal fee, java.math.BigDecimal freeShippingThreshold, Integer estimatedMinutes, Integer displayOrder, boolean enabled) {}

    List<Setting> getAllSettings();

    Setting updateSetting(DeliveryMethod deliveryMethod, DeliverySettingRequest request);
}
