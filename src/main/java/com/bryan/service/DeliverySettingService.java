package com.bryan.service;

import java.util.List;

public interface DeliverySettingService {
    record Setting(String deliveryType, java.math.BigDecimal fee, java.math.BigDecimal freeShippingThreshold, Integer estimatedMinutes, Integer displayOrder, boolean enabled) {}

    List<Setting> getAllSettings();
}
