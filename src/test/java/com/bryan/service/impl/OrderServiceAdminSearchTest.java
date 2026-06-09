package com.bryan.service.impl;

import com.bryan.mapper.OrderMapper;
import com.bryan.repository.CartRepository;
import com.bryan.repository.DeliverySlotRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
import com.bryan.service.AuditLogService;
import com.bryan.service.DeliverySettingService;
import com.bryan.service.PromotionRedemptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceAdminSearchTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock UserAddressRepository userAddressRepository;
    @Mock InventoryBatchRepository batchRepository;
    @Mock DeliverySlotRepository deliverySlotRepository;
    @Mock OrderMapper orderMapper;
    @Mock DeliverySettingService deliverySettingService;
    @Mock PromotionRedemptionService promotionRedemptionService;
    @Mock AuditLogService auditLogService;

    @InjectMocks OrderServiceImpl service;

    @Test
    void shouldUseDynamicSpecificationWhenAdminOrderFiltersAreEmpty() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.searchOrders(null, null, null, null, null, pageable);

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
}
