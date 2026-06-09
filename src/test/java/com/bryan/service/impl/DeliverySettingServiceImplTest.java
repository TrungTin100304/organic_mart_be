package com.bryan.service.impl;

import com.bryan.config.InternalDeliveryProperties;
import com.bryan.entity.DeliverySetting;
import com.bryan.entity.DeliverySetting.DeliveryType;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.DeliverySettingRepository;
import com.bryan.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverySettingServiceImplTest {

    @Mock DeliverySettingRepository repository;
    @Mock AuditLogService auditLogService;
    @Mock InternalDeliveryProperties fallbackProperties;
    @InjectMocks DeliverySettingServiceImpl service;

    @Test
    void returnsDatabaseFeeForEnabledMethod() {
        DeliverySetting setting = setting(true, "15000");
        when(repository.findByDeliveryType(DeliveryType.EXPRESS)).thenReturn(Optional.of(setting));

        assertEquals(new BigDecimal("15000"), service.getActiveFee(DeliveryType.EXPRESS));
    }

    @Test
    void rejectsDisabledDeliveryMethodInsteadOfFallingBackToProperties() {
        DeliverySetting setting = setting(false, "15000");
        when(repository.findByDeliveryType(DeliveryType.EXPRESS)).thenReturn(Optional.of(setting));

        assertThrows(BadRequestException.class, () -> service.getActiveFee(DeliveryType.EXPRESS));
    }

    @Test
    void returnsZeroWhenSubtotalReachesFreeShippingThreshold() {
        DeliverySetting setting = setting(true, "15000");
        setting.setFreeShippingThreshold(new BigDecimal("200000"));
        when(repository.findByDeliveryType(DeliveryType.EXPRESS)).thenReturn(Optional.of(setting));

        assertEquals(BigDecimal.ZERO,
                service.calculateFee(DeliveryType.EXPRESS, new BigDecimal("200000")));
    }

    private DeliverySetting setting(boolean enabled, String fee) {
        DeliverySetting setting = new DeliverySetting();
        setting.setDeliveryType(DeliveryType.EXPRESS);
        setting.setEnabled(enabled);
        setting.setFee(new BigDecimal(fee));
        return setting;
    }
}
