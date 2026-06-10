package com.bryan.service.impl;

import com.bryan.entity.Order;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.mapper.OrderMapper;
import com.bryan.repository.CartRepository;
import com.bryan.repository.DeliverySlotRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.PromotionRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
import com.bryan.service.DeliverySettingService;
import com.bryan.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceSecurityTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock UserAddressRepository userAddressRepository;
    @Mock InventoryBatchRepository batchRepository;
    @Mock PromotionRepository promotionRepository;
    @Mock DeliverySlotRepository deliverySlotRepository;
    @Mock OrderMapper orderMapper;
    @Mock DeliverySettingService deliverySettingService;

    @InjectMocks OrderServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void regularUserCannotReadAnotherUsersOrderByIdOrCode() {
        authenticate(1L, "ROLE_USER");
        Order otherUsersOrder = orderOwnedBy(2L);
        when(orderRepository.findById(9L)).thenReturn(Optional.of(otherUsersOrder));
        when(orderRepository.findByOrderCode("ORD-OTHER")).thenReturn(Optional.of(otherUsersOrder));

        assertThrows(BadRequestException.class, () -> service.getOrderById(9L));
        assertThrows(BadRequestException.class, () -> service.getOrderByCode("ORD-OTHER"));
    }

    private void authenticate(Long userId, String role) {
        CustomUserDetails principal = new CustomUserDetails(
                userId, "user@test.dev", "password", List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Order orderOwnedBy(Long userId) {
        User owner = new User();
        owner.setId(userId);
        Order order = new Order();
        order.setUser(owner);
        return order;
    }
}
