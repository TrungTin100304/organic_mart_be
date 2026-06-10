package com.bryan.service.impl;

import com.bryan.config.VietQrProperties;
import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.mapper.OrderMapper;
import com.bryan.repository.*;
import com.bryan.service.DeliverySettingService;
import com.bryan.service.PromotionRedemptionService;
import com.bryan.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VietQrPaymentCompleteOrderTest {

    @Mock private PaymentRequestRepository paymentRequestRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserAddressRepository userAddressRepository;
    @Mock private DeliverySlotRepository deliverySlotRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryBatchRepository inventoryBatchRepository;
    @Mock private VietQrProperties vietQrProperties;
    @Mock private DeliverySettingService deliverySettingService;
    @Mock private PromotionRedemptionService promotionRedemptionService;
    @Mock private ObjectMapper objectMapper;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private VietQrPaymentServiceImpl service;

    private User user;
    private UserAddress address;
    private ResidentialBuilding building;
    private PaymentRequest paidPayment;
    private String validSnapshot;

    @BeforeEach
    void setUp() throws Exception {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@test.com");

        building = new ResidentialBuilding();
        building.setId(1L);
        building.setCode("A");
        building.setName("Tower A");
        building.setIsActive(true);

        address = new UserAddress();
        address.setId(5L);
        address.setUser(user);
        address.setRecipientName("Test User");
        address.setRecipientPhone("0909000000");
        address.setFullAddress("123 Test St");
        address.setWard("Ward 1");
        address.setDistrict("Dist 1");
        address.setCity("HCM");
        address.setBuilding(building);
        address.setFloor("5");
        address.setApartmentNumber("501");

        validSnapshot = "[{\"productId\":3,\"productName\":\"Rau muống\",\"quantity\":2,\"priceAtPayment\":100000}]";

        paidPayment = newPayment("OMPAID12PAYMENT", new BigDecimal("220000"), PaymentStatus.PAID);
        paidPayment.setTransactionId("sepay-txn-1");
        paidPayment.setPaidAt(java.time.LocalDateTime.now());
        paidPayment.setShippingFee(new BigDecimal("20000"));
        paidPayment.setSubtotal(new BigDecimal("200000"));
        paidPayment.setPaymentItemsSnapshot(validSnapshot);
        paidPayment.setDeliveryMethod(DeliveryMethod.STANDARD);
        paidPayment.setBuildingCodeSnapshot("A");
        paidPayment.setBuildingNameSnapshot("Tower A");
        paidPayment.setFloorSnapshot("5");
        paidPayment.setApartmentNumberSnapshot("501");
        paidPayment.setRecipientNameSnapshot("Test User");
        paidPayment.setRecipientPhoneSnapshot("0909000000");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        CustomUserDetails principal = new CustomUserDetails(
            user.getId(), user.getEmail(), "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(orderRepository.existsByPaymentRequestId(any())).thenReturn(false);
        lenient().when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        List<VietQrPaymentServiceImpl.PaymentItemSnapshot> parsedItems = List.of(
            new VietQrPaymentServiceImpl.PaymentItemSnapshot(3L, "Rau muống",
                new BigDecimal("2"), new BigDecimal("100000"))
        );
        lenient().when(objectMapper.readValue(eq(validSnapshot), any(tools.jackson.core.type.TypeReference.class)))
            .thenReturn(parsedItems);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private OrderResponse fakeOrderResponse(String code) {
        return new OrderResponse(
            100L, code, 1L, "Buyer", 5L, null,
            "Test User", "0909000000", "Căn hộ 501, tầng 5, tòa A", null,
            null,
            new BigDecimal("200000"), BigDecimal.ZERO, new BigDecimal("20000"),
            new BigDecimal("220000"), OrderStatus.PENDING, null,
            List.of(), List.of(), null, null,
            DeliveryMethod.STANDARD, null, null, null,
            "A", "Tower A", "5", "501", "Test User", "0909000000", null
        );
    }

    // ─── Happy Path ───────────────────────────────────────────────────

    @Nested
    class HappyPath {

        @Test
        void shouldCreateOrderFromPaidPayment() throws Exception {
            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(any()))
                .thenReturn(Optional.empty());
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD123456789"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(100L);
                o.setOrderCode("ORD123456789");
                return o;
            });

            OrderResponse result = service.completeOrderFromPayment(paidPayment.getId());

            assertNotNull(result);
            assertEquals("ORD123456789", result.orderCode());
            verify(orderRepository).save(any(Order.class));
            assertNotNull(paidPayment.getOrder());
        }

        @Test
        void shouldCreateOrderFromTrustedWebhookWithoutUserSession() throws Exception {
            SecurityContextHolder.clearContext();
            when(paymentRequestRepository.findById(paidPayment.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(any()))
                .thenReturn(Optional.empty());
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD-WEBHOOK"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order order = i.getArgument(0);
                order.setId(101L);
                order.setOrderCode("ORD-WEBHOOK");
                return order;
            });

            OrderResponse result = service.completeOrderFromConfirmedPayment(paidPayment.getId());

            assertEquals("ORD-WEBHOOK", result.orderCode());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        void shouldLinkPaymentToOrder() throws Exception {
            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(any()))
                .thenReturn(Optional.empty());
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD123"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(100L);
                o.setOrderCode("ORD123");
                return o;
            });

            service.completeOrderFromPayment(paidPayment.getId());

            ArgumentCaptor<PaymentRequest> captor = ArgumentCaptor.forClass(PaymentRequest.class);
            verify(paymentRequestRepository, atLeastOnce()).save(captor.capture());
            assertNotNull(captor.getValue().getOrder());
        }

        @Test
        void shouldClearCartAfterCompletingOrder() throws Exception {
            Cart cartWithItems = new Cart();
            cartWithItems.setUser(user);
            cartWithItems.setItems(new ArrayList<>());

            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(any()))
                .thenReturn(Optional.empty());
            when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cartWithItems));
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD123"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(100L);
                o.setOrderCode("ORD123");
                return o;
            });

            service.completeOrderFromPayment(paidPayment.getId());

            assertTrue(cartWithItems.getItems().isEmpty());
            verify(cartRepository).save(cartWithItems);
        }

        @Test
        void shouldReduceInventoryOnOrderCompletion() throws Exception {
            InventoryBatch batch = new InventoryBatch();
            batch.setId(1L);
            batch.setQuantityRemaining(new BigDecimal("10"));

            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(3L))
                .thenReturn(Optional.of(batch));
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD123"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(100L);
                o.setOrderCode("ORD123");
                return o;
            });

            service.completeOrderFromPayment(paidPayment.getId());

            assertEquals(new BigDecimal("8"), batch.getQuantityRemaining());
            verify(inventoryBatchRepository).save(batch);
        }
    }

    // ─── Idempotency ───────────────────────────────────────────────

    @Nested
    class Idempotency {

        @Test
        void shouldReturnExistingOrderWhenCalledAgain() throws Exception {
            Order existingOrder = new Order();
            existingOrder.setId(200L);
            existingOrder.setOrderCode("EXISTING-ORDER");
            existingOrder.setUser(user);
            existingOrder.setAddress(address);
            existingOrder.setStatus(OrderStatus.PENDING);
            existingOrder.setSubtotal(new BigDecimal("200000"));
            existingOrder.setDiscountAmount(BigDecimal.ZERO);
            existingOrder.setShippingFee(new BigDecimal("20000"));
            existingOrder.setTotalAmount(new BigDecimal("220000"));
            existingOrder.setShippingRecipientSnapshot("T");
            existingOrder.setShippingPhoneSnapshot("0");
            existingOrder.setShippingAddressSnapshot("A");
            existingOrder.setShippingProviderNameSnapshot(null);
            paidPayment.setOrder(existingOrder);

            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(orderMapper.toResponse(existingOrder)).thenReturn(fakeOrderResponse("EXISTING-ORDER"));

            OrderResponse result = service.completeOrderFromPayment(paidPayment.getId());

            assertEquals("EXISTING-ORDER", result.orderCode());
            verify(orderRepository, never()).save(any());
        }
    }

    // ─── Rejection Cases ───────────────────────────────────────────

    @Nested
    class RejectionCases {

        @Test
        void shouldRejectPaymentNotFound() {
            when(paymentRequestRepository.findByIdAndUserId(999L, user.getId()))
                .thenReturn(Optional.empty());

            assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(999L));
        }

        @Test
        void shouldRejectPendingPayment() {
            PaymentRequest pending = newPayment("OMPENDING123456", new BigDecimal("220000"), PaymentStatus.PENDING);
            when(paymentRequestRepository.findByIdAndUserId(pending.getId(), user.getId()))
                .thenReturn(Optional.of(pending));

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(pending.getId()));
            assertTrue(ex.getMessage().toLowerCase().contains("paid"));
        }

        @Test
        void shouldRejectExpiredPayment() {
            PaymentRequest expired = newPayment("OMEXPIREDPAYMENT", new BigDecimal("220000"), PaymentStatus.EXPIRED);
            when(paymentRequestRepository.findByIdAndUserId(expired.getId(), user.getId()))
                .thenReturn(Optional.of(expired));

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(expired.getId()));
            assertTrue(ex.getMessage().toLowerCase().contains("paid"));
        }

        @Test
        void shouldRejectCancelledPayment() {
            PaymentRequest cancelled = newPayment("OMCANCELLEDPAYMENT", new BigDecimal("220000"), PaymentStatus.CANCELLED);
            when(paymentRequestRepository.findByIdAndUserId(cancelled.getId(), user.getId()))
                .thenReturn(Optional.of(cancelled));

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(cancelled.getId()));
            assertTrue(ex.getMessage().toLowerCase().contains("paid"));
        }

        @Test
        void shouldRejectDuplicateOrderViaUniqueConstraint() {
            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(orderRepository.existsByPaymentRequestId(paidPayment.getId())).thenReturn(true);

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(paidPayment.getId()));
            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        void shouldRejectPaymentBelongingToAnotherUser() {
            when(paymentRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(Optional.empty());

            assertThrows(BadRequestException.class,
                () -> service.completeOrderFromPayment(777L));
        }
    }

    // ─── Cart Snapshot ──────────────────────────────────────────────

    @Nested
    class CartSnapshot {

        @Test
        void shouldUsePaymentSnapshotNotCurrentCart() throws Exception {
            when(paymentRequestRepository.findByIdAndUserId(paidPayment.getId(), user.getId()))
                .thenReturn(Optional.of(paidPayment));
            when(inventoryBatchRepository.findFirstByProductIdOrderByExpiryDateAsc(any()))
                .thenReturn(Optional.empty());
            when(orderMapper.toResponse(any())).thenReturn(fakeOrderResponse("ORD123"));
            when(orderRepository.save(any())).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(100L);
                o.setOrderCode("ORD123");
                return o;
            });

            service.completeOrderFromPayment(paidPayment.getId());

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order saved = captor.getValue();

            assertEquals(paidPayment.getSubtotal(), saved.getSubtotal());
            assertEquals(paidPayment.getShippingFee(), saved.getShippingFee());
            assertEquals(paidPayment.getAmount(), saved.getTotalAmount());
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private PaymentRequest newPayment(String code, BigDecimal amount, PaymentStatus status) {
        PaymentRequest p = new PaymentRequest();
        p.setId(10L);
        p.setUser(user);
        p.setAddress(address);
        p.setSubtotal(amount.subtract(new BigDecimal("20000")));
        p.setShippingFee(new BigDecimal("20000"));
        p.setAmount(amount);
        p.setTransferCode(code);
        p.setQrUrl("https://img.vietqr.io/test.png");
        p.setStatus(status);
        p.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(30));
        return p;
    }
}
