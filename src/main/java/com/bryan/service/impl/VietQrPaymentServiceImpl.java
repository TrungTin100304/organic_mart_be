package com.bryan.service.impl;

import com.bryan.config.VietQrProperties;
import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.request.VietQrWebhookRequest;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.User;
import com.bryan.entity.UserAddress;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.CartRepository;
import com.bryan.repository.PaymentRequestRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.VietQrPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VietQrPaymentServiceImpl implements VietQrPaymentService {

    private static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("20000");
    private static final BigDecimal EXPRESS_SHIPPING_FEE = new BigDecimal("50000");

    private final PaymentRequestRepository paymentRequestRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final VietQrProperties properties;

    @Override
    public VietQrPaymentResponse createPayment(VietQrPaymentRequest request) {
        validateConfiguration();
        User user = getAuthenticatedUser();
        UserAddress address = getOwnedAddress(request.addressId(), user);
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        BigDecimal subtotal = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = shippingFeeFor(request.shippingMethod());
        BigDecimal amount = subtotal.add(shippingFee);
        String transferCode = "OM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        String qrUrl = buildQrUrl(amount, transferCode);

        PaymentRequest payment = new PaymentRequest();
        payment.setUser(user);
        payment.setAddress(address);
        payment.setSubtotal(subtotal);
        payment.setShippingFee(shippingFee);
        payment.setAmount(amount);
        payment.setTransferCode(transferCode);
        payment.setQrUrl(qrUrl);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        return toResponse(paymentRequestRepository.save(payment));
    }

    @Override
    public VietQrPaymentResponse getPayment(Long id) {
        User user = getAuthenticatedUser();
        PaymentRequest payment = paymentRequestRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment request not found: " + id));
        return toResponse(expireIfNeeded(payment));
    }

    @Override
    public VietQrPaymentResponse confirmPayment(VietQrWebhookRequest request, String webhookSecret) {
        validateWebhookSecret(webhookSecret);
        PaymentRequest payment = paymentRequestRepository.findByTransferCode(request.transferCode().trim().toUpperCase(Locale.ROOT))
            .orElseThrow(() -> new ResourceNotFoundException("Payment request not found for transfer code"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            if (request.transactionId().equals(payment.getTransactionId())) {
                return toResponse(payment);
            }
            throw new BadRequestException("Payment request is already paid");
        }

        payment = expireIfNeeded(payment);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment request is no longer pending");
        }
        if (payment.getAmount().compareTo(request.amount()) != 0) {
            throw new BadRequestException("Transferred amount does not match payment amount");
        }
        if (paymentRequestRepository.existsByTransactionId(request.transactionId())) {
            throw new BadRequestException("Transaction has already been processed");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.transactionId());
        payment.setPaidAt(LocalDateTime.now());
        return toResponse(paymentRequestRepository.save(payment));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("Authentication required");
        }
        return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));
    }

    private UserAddress getOwnedAddress(Long addressId, User user) {
        UserAddress address = userAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("User address not found: " + addressId));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to use this address");
        }
        return address;
    }

    private BigDecimal shippingFeeFor(String shippingMethod) {
        return switch (shippingMethod.toUpperCase(Locale.ROOT)) {
            case "STANDARD" -> STANDARD_SHIPPING_FEE;
            case "EXPRESS" -> EXPRESS_SHIPPING_FEE;
            default -> throw new BadRequestException("Unsupported shipping method");
        };
    }

    private String buildQrUrl(BigDecimal amount, String transferCode) {
        String accountName = URLEncoder.encode(properties.getAccountName(), StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s".formatted(
            properties.getBankId(),
            properties.getAccountNo(),
            properties.getTemplate(),
            amount.longValueExact(),
            transferCode,
            accountName
        );
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getBankId())
            || !StringUtils.hasText(properties.getAccountNo())
            || !StringUtils.hasText(properties.getAccountName())
            || !StringUtils.hasText(properties.getTemplate())) {
            throw new BadRequestException("VietQR payment is not configured");
        }
    }

    private void validateWebhookSecret(String providedSecret) {
        if (!StringUtils.hasText(properties.getWebhookSecret()) || !StringUtils.hasText(providedSecret)) {
            throw new BadRequestException("Invalid payment webhook secret");
        }
        boolean matches = MessageDigest.isEqual(
            properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
            providedSecret.getBytes(StandardCharsets.UTF_8)
        );
        if (!matches) {
            throw new BadRequestException("Invalid payment webhook secret");
        }
    }

    private PaymentRequest expireIfNeeded(PaymentRequest payment) {
        if (payment.getStatus() == PaymentStatus.PENDING
            && payment.getExpiresAt() != null
            && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            return paymentRequestRepository.save(payment);
        }
        return payment;
    }

    private VietQrPaymentResponse toResponse(PaymentRequest payment) {
        return new VietQrPaymentResponse(
            payment.getId(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getTransferCode(),
            payment.getQrUrl(),
            properties.getBankId(),
            properties.getAccountNo(),
            properties.getAccountName(),
            payment.getExpiresAt()
        );
    }
}
