package com.bryan.service.impl;

import com.bryan.config.VietQrProperties;
import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.request.VietQrWebhookRequest;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.CartItem;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.Product;
import com.bryan.entity.User;
import com.bryan.entity.UserAddress;
import com.bryan.repository.CartRepository;
import com.bryan.repository.PaymentRequestRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VietQrPaymentServiceImplTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private VietQrProperties properties;

    @InjectMocks
    private VietQrPaymentServiceImpl service;

    private User user;
    private UserAddress address;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@example.com");

        address = new UserAddress();
        address.setId(5L);
        address.setUser(user);

        CustomUserDetails principal = new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(properties.getBankId()).thenReturn("970436");
        lenient().when(properties.getAccountNo()).thenReturn("123456789");
        lenient().when(properties.getAccountName()).thenReturn("ORGANIC MART");
        lenient().when(properties.getTemplate()).thenReturn("compact2");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateVietQrFromAuthenticatedUsersCart() {
        when(userAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(paymentRequestRepository.save(any())).thenAnswer(invocation -> {
            var payment = invocation.getArgument(0, com.bryan.entity.PaymentRequest.class);
            payment.setId(10L);
            return payment;
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
            new VietQrPaymentRequest(address.getId(), "STANDARD")
        );

        assertEquals(new BigDecimal("220000"), result.amount());
        assertEquals("PENDING", result.status());
        assertTrue(result.transferCode().startsWith("OM"));
        assertTrue(result.qrUrl().startsWith("https://img.vietqr.io/image/970436-123456789-compact2.png"));
        assertTrue(result.qrUrl().contains("amount=220000"));
        assertTrue(result.qrUrl().contains("addInfo=" + result.transferCode()));
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
            () -> service.createPayment(new VietQrPaymentRequest(address.getId(), "STANDARD"))
        );
    }

    @Test
    void shouldExpirePendingPaymentWhenFetchingAfterExpiry() {
        PaymentRequest payment = payment("OMEXPIRED", new BigDecimal("220000"));
        payment.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(paymentRequestRepository.findByIdAndUserId(payment.getId(), user.getId())).thenReturn(Optional.of(payment));
        when(paymentRequestRepository.save(payment)).thenReturn(payment);

        VietQrPaymentResponse result = service.getPayment(payment.getId());

        assertEquals("EXPIRED", result.status());
        verify(paymentRequestRepository).save(payment);
    }

    @Test
    void shouldMarkPaymentPaidFromValidWebhook() {
        PaymentRequest payment = payment("OMPAID123", new BigDecimal("220000"));
        when(properties.getWebhookSecret()).thenReturn("webhook-secret");
        when(paymentRequestRepository.findByTransferCode(payment.getTransferCode())).thenReturn(Optional.of(payment));
        when(paymentRequestRepository.existsByTransactionId("TXN-001")).thenReturn(false);
        when(paymentRequestRepository.save(payment)).thenReturn(payment);

        VietQrPaymentResponse result = service.confirmPayment(
            new VietQrWebhookRequest(payment.getTransferCode(), payment.getAmount(), "TXN-001"),
            "webhook-secret"
        );

        assertEquals("PAID", result.status());
        assertEquals("TXN-001", payment.getTransactionId());
        assertTrue(payment.getPaidAt() != null);
    }

    private PaymentRequest payment(String transferCode, BigDecimal amount) {
        PaymentRequest payment = new PaymentRequest();
        payment.setId(10L);
        payment.setUser(user);
        payment.setAddress(address);
        payment.setSubtotal(amount.subtract(new BigDecimal("20000")));
        payment.setShippingFee(new BigDecimal("20000"));
        payment.setAmount(amount);
        payment.setTransferCode(transferCode);
        payment.setQrUrl("https://img.vietqr.io/image/test.png");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        return payment;
    }
}
