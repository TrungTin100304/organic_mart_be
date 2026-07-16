package com.bryan.service.impl;

import com.bryan.dto.request.CreateOrderRequest;
import com.bryan.dto.request.OrderItemRequest;
import com.bryan.dto.request.UpdateOrderStatusRequest;
import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.CartItem;
import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.InventoryBatch;
import com.bryan.entity.Order;
import com.bryan.entity.OrderDetail;
import com.bryan.entity.OrderStatus;
import com.bryan.entity.Product;
import com.bryan.entity.Promotion;
import com.bryan.entity.ResidentialBuilding;
import com.bryan.entity.User;
import com.bryan.entity.UserAddress;
import com.bryan.mapper.OrderMapper;
import com.bryan.repository.CartRepository;
import com.bryan.repository.DeliverySlotRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.DeliverySettingService;
import com.bryan.service.PromotionRedemptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutIntegrityTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock UserRepository userRepository;
    @Mock UserAddressRepository userAddressRepository;
    @Mock InventoryBatchRepository batchRepository;
    @Mock InventoryAllocationService inventoryAllocationService;
    @Mock PromotionRedemptionService promotionRedemptionService;
    @Mock DeliverySlotRepository deliverySlotRepository;
    @Mock OrderMapper orderMapper;
    @Mock DeliverySettingService deliverySettingService;

    @InjectMocks OrderServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void codCheckoutUsesPromotionReservationAndLockedInventoryAllocation() {
        User user = new User();
        user.setId(1L);
        user.setEmail("buyer@test.dev");
        authenticate(user);

        ResidentialBuilding building = new ResidentialBuilding();
        building.setCode("S1");
        building.setName("S1");
        building.setIsActive(true);

        UserAddress address = new UserAddress();
        address.setId(5L);
        address.setUser(user);
        address.setBuilding(building);
        address.setFloor("5");
        address.setApartmentNumber("501");
        address.setRecipientName("Buyer");
        address.setRecipientPhone("0900000000");

        Product product = new Product();
        product.setId(9L);
        product.setName("Organic product");
        product.setPrice(new BigDecimal("100000"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(new BigDecimal("2"));
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(item)));

        Promotion promotion = new Promotion();
        promotion.setId(3L);
        promotion.setCode("SAVE10");
        var applied = new PromotionRedemptionService.AppliedPromotion(
            promotion, new BigDecimal("10000"));

        InventoryBatch batch = new InventoryBatch();
        batch.setId(7L);
        batch.setProduct(product);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(userAddressRepository.findById(5L)).thenReturn(Optional.of(address));
        when(deliverySettingService.calculateFee(DeliveryMethod.STANDARD, new BigDecimal("200000")))
            .thenReturn(BigDecimal.ZERO);
        when(promotionRedemptionService.reserve("SAVE10", user, new BigDecimal("200000")))
            .thenReturn(applied);
        when(inventoryAllocationService.allocate(9L, new BigDecimal("2")))
            .thenReturn(List.of(new InventoryAllocationService.Allocation(batch, new BigDecimal("2"))));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(emptyResponse());

        service.createOrderFromCart(new CreateOrderRequest(
            5L,
            "SAVE10",
            null,
            List.of(new OrderItemRequest(9L, new BigDecimal("2"))),
            DeliveryMethod.STANDARD,
            null,
            null));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertEquals(promotion, saved.getPromotion());
        assertEquals(new BigDecimal("10000"), saved.getDiscountAmount());
        assertEquals(new BigDecimal("190000"), saved.getTotalAmount());
        assertEquals(batch, saved.getDetails().iterator().next().getBatch());
        verify(promotionRedemptionService).recordUsage(applied, user, saved, null);
    }

    @Test
    void adminCancellationLocksOrderAndRestoresInventory() {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.dev");
        authenticate(admin, "ROLE_ADMIN");

        InventoryBatch batch = new InventoryBatch();
        batch.setId(7L);
        batch.setQuantityRemaining(new BigDecimal("3"));

        OrderDetail detail = new OrderDetail();
        detail.setBatch(batch);
        detail.setQuantity(new BigDecimal("2"));
        detail.setPriceAtPurchase(BigDecimal.ONE);

        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.PENDING);
        order.addDetail(detail);

        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(emptyResponse());

        service.updateOrderStatus(10L, new UpdateOrderStatusRequest(OrderStatus.CANCELLED, "Admin cancelled"));

        assertEquals(new BigDecimal("5"), batch.getQuantityRemaining());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(batchRepository).save(batch);
        verify(promotionRedemptionService).releaseOrder(order);
    }

    private void authenticate(User user) {
        authenticate(user, "ROLE_USER");
    }

    private void authenticate(User user, String role) {
        CustomUserDetails principal = new CustomUserDetails(
            user.getId(), user.getEmail(), "password",
            List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private OrderResponse emptyResponse() {
        return new OrderResponse(
            null, null, null, null, null, null,
            null, null, null, null, null,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            null, null, List.of(), List.of(), null, null,
            null, null, null, null, null, null, null, null, null, null, null);
    }
}
