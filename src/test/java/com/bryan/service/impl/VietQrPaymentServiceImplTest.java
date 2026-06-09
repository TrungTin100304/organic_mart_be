package com.bryan.service.impl;

import com.bryan.config.VietQrProperties;
import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.entity.*;
import com.bryan.repository.*;
import com.bryan.service.DeliverySettingService;
import com.bryan.service.PromotionRedemptionService;
import com.bryan.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VietQrPaymentServiceImplTest {

    @Mock private PaymentRequestRepository paymentRequestRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserAddressRepository userAddressRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryBatchRepository inventoryBatchRepository;
    @Mock private DeliverySlotRepository deliverySlotRepository;
    @Mock private VietQrProperties vietQrProperties;
    @Mock private DeliverySettingService deliverySettingService;
    @Mock private PromotionRedemptionService promotionRedemptionService;
    @Mock private ObjectMapper objectMapper;
    @Mock private com.bryan.mapper.OrderMapper orderMapper;

    @InjectMocks
    private VietQrPaymentServiceImpl service;

    private User user;
    private UserAddress address;
    private ResidentialBuilding building;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@example.com");

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
        address.setBuilding(building);
        address.setFloor("5");
        address.setApartmentNumber("501");

        CustomUserDetails principal = new CustomUserDetails(
            user.getId(), user.getEmail(), "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(vietQrProperties.getBankId()).thenReturn("970423");
        lenient().when(vietQrProperties.getAccountNo()).thenReturn("123456789");
        lenient().when(vietQrProperties.getAccountName()).thenReturn("ORGANIC MART");
        lenient().when(vietQrProperties.getTemplate()).thenReturn("compact2");
        lenient().when(deliverySettingService.calculateFee(eq(DeliverySetting.DeliveryType.STANDARD), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        lenient().when(deliverySettingService.calculateFee(eq(DeliverySetting.DeliveryType.EXPRESS), any(BigDecimal.class))).thenReturn(new BigDecimal("20000"));
        lenient().when(deliverySettingService.calculateFee(eq(DeliverySetting.DeliveryType.SCHEDULED), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateVietQrWithStandardDelivery() throws Exception {
        when(userAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(paymentRequestRepository.save(any())).thenAnswer(inv -> {
            com.bryan.entity.PaymentRequest p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        Product product = new Product();
        product.setPrice(new BigDecimal("100000"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(new BigDecimal("2"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(List.of(item));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        VietQrPaymentResponse result = service.createPayment(
            new VietQrPaymentRequest(address.getId(), DeliveryMethod.STANDARD, null, null, null)
        );

        assertEquals(new BigDecimal("200000"), result.amount());
        assertEquals("PENDING", result.status());
        assertTrue(result.transferCode().startsWith("OM"));
    }

    @Test
    void shouldCreateVietQrWhenAccountNameIsBlank() throws Exception {
        when(vietQrProperties.getAccountName()).thenReturn("");
        when(userAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(paymentRequestRepository.save(any())).thenAnswer(inv -> {
            PaymentRequest payment = inv.getArgument(0);
            payment.setId(11L);
            return payment;
        });

        Product product = new Product();
        product.setPrice(new BigDecimal("50000"));
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(BigDecimal.ONE);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(List.of(item));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        VietQrPaymentResponse result = service.createPayment(
            new VietQrPaymentRequest(address.getId(), DeliveryMethod.STANDARD, null, null, null)
        );

        assertTrue(result.qrUrl().startsWith("https://img.vietqr.io/image/970423-123456789-compact2.png"));
        assertFalse(result.qrUrl().contains("accountName="));
    }

    @Test
    void shouldRejectEmptyCart() {
        when(userAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(List.of());
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(
            com.bryan.exception.BadRequestException.class,
            () -> service.createPayment(new VietQrPaymentRequest(address.getId(), DeliveryMethod.STANDARD, null, null, null))
        );
    }

    @Test
    void shouldExpirePendingPaymentWhenFetchingAfterExpiry() {
        com.bryan.entity.PaymentRequest payment = payment("OMEXPIRED", new BigDecimal("200000"));
        payment.setExpiresAt(java.time.LocalDateTime.now().minusMinutes(1));
        when(paymentRequestRepository.findByIdAndUserId(payment.getId(), user.getId())).thenReturn(Optional.of(payment));
        when(paymentRequestRepository.save(payment)).thenReturn(payment);

        VietQrPaymentResponse result = service.getPayment(payment.getId());

        assertEquals("EXPIRED", result.status());
        verify(paymentRequestRepository).save(payment);
    }

    @Test
    void shouldRejectPaymentBelongingToAnotherUser() {
        when(paymentRequestRepository.findByIdAndUserId(999L, user.getId())).thenReturn(Optional.empty());

        assertThrows(
            com.bryan.exception.ResourceNotFoundException.class,
            () -> service.getPayment(999L)
        );
    }

    @Test
    void shouldRejectAddressWithoutBuilding() {
        UserAddress addrNoBuilding = new UserAddress();
        addrNoBuilding.setId(6L);
        addrNoBuilding.setUser(user);
        addrNoBuilding.setRecipientName("T");
        addrNoBuilding.setRecipientPhone("0909");
        addrNoBuilding.setFullAddress("No building");

        when(userAddressRepository.findById(addrNoBuilding.getId())).thenReturn(Optional.of(addrNoBuilding));
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setProduct(new Product());
        item.getProduct().setPrice(new BigDecimal("50000"));
        item.setQuantity(new BigDecimal("1"));
        cart.setItems(List.of(item));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(
            com.bryan.exception.BadRequestException.class,
            () -> service.createPayment(new VietQrPaymentRequest(addrNoBuilding.getId(), DeliveryMethod.STANDARD, null, null, null))
        );
    }

    private com.bryan.entity.PaymentRequest payment(String transferCode, BigDecimal amount) {
        com.bryan.entity.PaymentRequest payment = new com.bryan.entity.PaymentRequest();
        payment.setId(10L);
        payment.setUser(user);
        payment.setAddress(address);
        payment.setSubtotal(amount);
        payment.setShippingFee(BigDecimal.ZERO);
        payment.setAmount(amount);
        payment.setTransferCode(transferCode);
        payment.setQrUrl("https://img.vietqr.io/image/test.png");
        payment.setStatus(com.bryan.entity.PaymentStatus.PENDING);
        payment.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(30));
        return payment;
    }
}
