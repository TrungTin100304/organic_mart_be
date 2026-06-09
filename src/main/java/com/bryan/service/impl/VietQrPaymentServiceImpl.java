package com.bryan.service.impl;

import com.bryan.config.InternalDeliveryProperties;
import com.bryan.config.VietQrProperties;
import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.response.OrderResponse;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.OrderMapper;
import com.bryan.repository.*;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.VietQrPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VietQrPaymentServiceImpl implements VietQrPaymentService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final InventoryBatchRepository batchRepository;
    private final DeliverySlotRepository deliverySlotRepository;
    private final VietQrProperties properties;
    private final InternalDeliveryProperties deliveryProperties;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

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

        validateInternalDeliveryAddress(address, request.deliveryMethod());
        validateDeliverySlot(request, address);

        BigDecimal subtotal = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = calculateShippingFee(request.deliveryMethod());
        BigDecimal amount = subtotal.add(shippingFee);

        String transferCode = "OM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        String qrUrl = buildQrUrl(amount, transferCode);

        String itemsSnapshot = snapshotCartItems(cart);

        String slotSnapshot = null;
        if (request.deliverySlotId() != null) {
            DeliverySlot slot = deliverySlotRepository.findById(request.deliverySlotId()).orElse(null);
            if (slot != null) slotSnapshot = slot.getName();
        }

        ResidentialBuilding building = address.getBuilding();
        String buildingCode = building != null ? building.getCode() : null;
        String buildingName = building != null ? building.getName() : null;

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
        payment.setPaymentItemsSnapshot(itemsSnapshot);

        // Internal delivery snapshots
        payment.setDeliveryMethod(request.deliveryMethod());
        payment.setDeliveryDate(request.deliveryDate());
        payment.setDeliverySlotId(request.deliverySlotId());
        payment.setDeliverySlotSnapshot(slotSnapshot);
        payment.setBuildingCodeSnapshot(buildingCode);
        payment.setBuildingNameSnapshot(buildingName);
        payment.setFloorSnapshot(address.getFloor());
        payment.setApartmentNumberSnapshot(address.getApartmentNumber());
        payment.setRecipientNameSnapshot(address.getRecipientName());
        payment.setRecipientPhoneSnapshot(address.getRecipientPhone());
        payment.setDeliveryNoteSnapshot(address.getDeliveryNote());

        return toResponse(paymentRequestRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public VietQrPaymentResponse getPayment(Long id) {
        User user = getAuthenticatedUser();
        PaymentRequest payment = paymentRequestRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment request not found: " + id));
        return toResponse(expireIfNeeded(payment));
    }

    @Override
    public OrderResponse completeOrderFromPayment(Long paymentId) {
        User user = getAuthenticatedUser();

        PaymentRequest payment = paymentRequestRepository.findByIdAndUserId(paymentId, user.getId())
            .orElseThrow(() -> new BadRequestException("Payment not found or access denied: " + paymentId));

        if (payment.getOrder() != null) {
            log.info("completeOrderFromPayment idempotent hit: paymentId={}, existingOrderId={}",
                paymentId, payment.getOrder().getId());
            return mapToOrderResponse(payment.getOrder());
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Payment must be PAID before creating an order. Current status: " + payment.getStatus());
        }

        if (orderRepository.existsByPaymentRequestId(paymentId)) {
            throw new BadRequestException("Order already exists for this payment");
        }

        List<PaymentItemSnapshot> snapshot = deserializeSnapshot(payment.getPaymentItemsSnapshot());
        if (snapshot.isEmpty()) {
            throw new BadRequestException("Payment has no item snapshot. Cannot create order.");
        }

        UserAddress address = payment.getAddress();

        BigDecimal subtotal = snapshot.stream()
            .map(i -> i.priceAtPayment().multiply(i.quantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPromotion(null);
        order.setOrderCode(generateOrderCode());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(payment.getShippingFee());
        order.setTotalAmount(payment.getAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setNote("Thanh toán VietQR - Mã: " + payment.getTransferCode());

        // Copy all internal delivery snapshots from payment
        order.setDeliveryMethod(payment.getDeliveryMethod());
        order.setDeliveryDate(payment.getDeliveryDate());
        order.setDeliverySlotId(payment.getDeliverySlotId());
        order.setDeliverySlotSnapshot(payment.getDeliverySlotSnapshot());
        order.setBuildingCodeSnapshot(payment.getBuildingCodeSnapshot());
        order.setBuildingNameSnapshot(payment.getBuildingNameSnapshot());
        order.setFloorSnapshot(payment.getFloorSnapshot());
        order.setApartmentNumberSnapshot(payment.getApartmentNumberSnapshot());
        order.setRecipientNameSnapshot(payment.getRecipientNameSnapshot());
        order.setRecipientPhoneSnapshot(payment.getRecipientPhoneSnapshot());
        order.setDeliveryNoteSnapshot(payment.getDeliveryNoteSnapshot());

        // Legacy snapshots
        order.setShippingRecipientSnapshot(payment.getRecipientNameSnapshot());
        order.setShippingPhoneSnapshot(payment.getRecipientPhoneSnapshot());
        order.setShippingAddressSnapshot(buildFullAddress(address));
        order.setShippingProviderNameSnapshot(null);

        for (PaymentItemSnapshot item : snapshot) {
            Product product = new Product();
            product.setId(item.productId());
            product.setName(item.productName());
            product.setPrice(item.priceAtPayment());

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(item.quantity());
            detail.setPriceAtPurchase(item.priceAtPayment());

            batchRepository.findFirstByProductIdOrderByExpiryDateAsc(item.productId())
                .filter(b -> b.getQuantityRemaining().compareTo(item.quantity()) >= 0)
                .ifPresent(b -> {
                    b.setQuantityRemaining(b.getQuantityRemaining().subtract(item.quantity()));
                    batchRepository.save(b);
                    detail.setBatch(b);
                });

            order.addDetail(detail);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.PENDING);
        history.setChangedBy(user);
        history.setNote("Order created from VietQR payment " + payment.getTransferCode());
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);

        payment.setOrder(savedOrder);
        paymentRequestRepository.save(payment);

        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });

        log.info("VietQR order created from payment: orderId={}, paymentId={}, code={}",
            savedOrder.getId(), paymentId, savedOrder.getOrderCode());
        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse completeOrderFromConfirmedPayment(Long paymentId) {
        PaymentRequest payment = paymentRequestRepository.findById(paymentId)
            .orElseThrow(() -> new BadRequestException("Payment not found: " + paymentId));

        if (payment.getOrder() != null) {
            log.info("completeOrderFromConfirmedPayment idempotent hit: paymentId={}, existingOrderId={}",
                paymentId, payment.getOrder().getId());
            return mapToOrderResponse(payment.getOrder());
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Payment must be PAID. Current status: " + payment.getStatus());
        }

        User user = payment.getUser();
        List<PaymentItemSnapshot> snapshot = deserializeSnapshot(payment.getPaymentItemsSnapshot());
        if (snapshot.isEmpty()) {
            throw new BadRequestException("Payment has no item snapshot");
        }

        UserAddress address = payment.getAddress();
        BigDecimal subtotal = snapshot.stream()
            .map(i -> i.priceAtPayment().multiply(i.quantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPromotion(null);
        order.setOrderCode(generateOrderCode());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(payment.getShippingFee());
        order.setTotalAmount(payment.getAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setNote("Thanh toán VietQR - Mã: " + payment.getTransferCode());
        order.setPaymentMethod(PaymentMethod.VIETQR);

        order.setDeliveryMethod(payment.getDeliveryMethod());
        order.setDeliveryDate(payment.getDeliveryDate());
        order.setDeliverySlotId(payment.getDeliverySlotId());
        order.setDeliverySlotSnapshot(payment.getDeliverySlotSnapshot());
        order.setBuildingCodeSnapshot(payment.getBuildingCodeSnapshot());
        order.setBuildingNameSnapshot(payment.getBuildingNameSnapshot());
        order.setFloorSnapshot(payment.getFloorSnapshot());
        order.setApartmentNumberSnapshot(payment.getApartmentNumberSnapshot());
        order.setRecipientNameSnapshot(payment.getRecipientNameSnapshot());
        order.setRecipientPhoneSnapshot(payment.getRecipientPhoneSnapshot());
        order.setDeliveryNoteSnapshot(payment.getDeliveryNoteSnapshot());

        order.setShippingRecipientSnapshot(payment.getRecipientNameSnapshot());
        order.setShippingPhoneSnapshot(payment.getRecipientPhoneSnapshot());
        order.setShippingAddressSnapshot(buildFullAddress(address));
        order.setShippingProviderNameSnapshot(null);

        for (PaymentItemSnapshot item : snapshot) {
            Product product = new Product();
            product.setId(item.productId());
            product.setName(item.productName());
            product.setPrice(item.priceAtPayment());

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(item.quantity());
            detail.setPriceAtPurchase(item.priceAtPayment());

            batchRepository.findFirstByProductIdOrderByExpiryDateAsc(item.productId())
                .filter(b -> b.getQuantityRemaining().compareTo(item.quantity()) >= 0)
                .ifPresent(b -> {
                    b.setQuantityRemaining(b.getQuantityRemaining().subtract(item.quantity()));
                    batchRepository.save(b);
                    detail.setBatch(b);
                });

            order.addDetail(detail);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.PENDING);
        history.setChangedBy(user);
        history.setNote("Order created from SePay webhook payment " + payment.getTransferCode());
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        payment.setOrder(savedOrder);
        paymentRequestRepository.save(payment);

        log.info("VietQR order created from confirmed payment: orderId={}, paymentId={}, code={}",
            savedOrder.getId(), paymentId, savedOrder.getOrderCode());
        return mapToOrderResponse(savedOrder);
    }

    // ─── Private helpers ───────────────────────────────────────────────────

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

    private void validateInternalDeliveryAddress(UserAddress address, DeliveryMethod method) {
        ResidentialBuilding building = address.getBuilding();
        if (building == null || !building.getIsActive()) {
            throw new BadRequestException(
                "Address must have an active building selected for internal delivery. Please update your address.");
        }
        if (address.getFloor() == null || address.getFloor().isBlank()) {
            throw new BadRequestException("Floor information is required for internal delivery address.");
        }
        if (address.getApartmentNumber() == null || address.getApartmentNumber().isBlank()) {
            throw new BadRequestException("Apartment number is required for internal delivery address.");
        }
    }

    private void validateDeliverySlot(VietQrPaymentRequest request, UserAddress address) {
        if (request.deliveryMethod() == DeliveryMethod.SCHEDULED) {
            if (request.deliveryDate() == null) {
                throw new BadRequestException("Delivery date is required for SCHEDULED delivery.");
            }
            if (request.deliveryDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Delivery date cannot be in the past.");
            }
            if (request.deliverySlotId() == null) {
                throw new BadRequestException("Delivery slot is required for SCHEDULED delivery.");
            }
            DeliverySlot slot = deliverySlotRepository.findById(request.deliverySlotId())
                .orElseThrow(() -> new BadRequestException("Delivery slot not found: " + request.deliverySlotId()));
            if (!slot.getIsActive()) {
                throw new BadRequestException("Delivery slot is not active: " + slot.getName());
            }
            long currentCount = deliverySlotRepository.countOrdersForSlotOnDate(slot.getId(), request.deliveryDate());
            if (currentCount >= slot.getMaximumOrders()) {
                throw new BadRequestException("Delivery slot is full: " + slot.getName());
            }
            // Check cutoff
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime slotStart = LocalDateTime.of(request.deliveryDate(), slot.getStartTime());
            LocalDateTime cutoff = slotStart.minusMinutes(slot.getCutoffMinutes());
            if (now.isAfter(cutoff)) {
                throw new BadRequestException("Too late to book slot " + slot.getName() + " (cutoff: " + slot.getCutoffMinutes() + " minutes before)");
            }
        }
    }

    private BigDecimal calculateShippingFee(DeliveryMethod method) {
        return switch (method) {
            case STANDARD -> deliveryProperties.getStandardFee();
            case EXPRESS -> deliveryProperties.getExpressFee();
            case SCHEDULED -> deliveryProperties.getScheduledFee();
        };
    }

    private String buildQrUrl(BigDecimal amount, String transferCode) {
        String qrUrl = "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s".formatted(
            properties.getBankId(),
            properties.getAccountNo(),
            properties.getTemplate(),
            amount.longValueExact(),
            transferCode
        );
        if (StringUtils.hasText(properties.getAccountName())) {
            qrUrl += "&accountName="
                + URLEncoder.encode(properties.getAccountName(), StandardCharsets.UTF_8);
        }
        return qrUrl;
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getBankId())
            || !StringUtils.hasText(properties.getAccountNo())
            || !StringUtils.hasText(properties.getTemplate())) {
            throw new BadRequestException("VietQR payment is not configured");
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
        Order order = payment.getOrder();
        return new VietQrPaymentResponse(
            payment.getId(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getTransferCode(),
            payment.getQrUrl(),
            properties.getBankId(),
            properties.getAccountNo(),
            properties.getAccountName(),
            payment.getExpiresAt(),
            payment.getPaidAt(),
            order != null ? order.getId() : null,
            order != null ? order.getOrderCode() : null
        );
    }

    private String snapshotCartItems(Cart cart) {
        List<PaymentItemSnapshot> items = cart.getItems().stream().map(item -> {
            Product p = item.getProduct();
            return new PaymentItemSnapshot(p.getId(), p.getName(), item.getQuantity(), p.getPrice());
        }).toList();
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JacksonException e) {
            log.error("Failed to serialize cart items snapshot", e);
            return "[]";
        }
    }

    List<PaymentItemSnapshot> deserializeSnapshot(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(snapshot, new TypeReference<>() {});
        } catch (JacksonException e) {
            log.error("Failed to deserialize payment items snapshot: {}", snapshot, e);
            return List.of();
        }
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "OM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private String buildFullAddress(UserAddress address) {
        if (address.getBuilding() != null) {
            return "Căn hộ " + address.getApartmentNumber()
                + ", tầng " + address.getFloor()
                + ", tòa " + address.getBuilding().getCode();
        }
        return List.of(
                address.getFullAddress(),
                address.getWard(),
                address.getDistrict(),
                address.getCity()
            ).stream()
            .filter(s -> s != null && !s.isBlank())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = orderMapper.toResponse(order);

        List<com.bryan.dto.response.OrderDetailResponse> details =
            order.getDetails().stream()
                .map(com.bryan.dto.response.OrderDetailResponse::from)
                .toList();

        List<com.bryan.dto.response.OrderStatusHistoryResponse> histories =
            order.getStatusHistories().stream()
                .map(com.bryan.dto.response.OrderStatusHistoryResponse::from)
                .toList();

        OrderResponse.PromotionSnapshotResponse promo = null;
        if (order.getPromotion() != null) {
            Promotion p = order.getPromotion();
            promo = new OrderResponse.PromotionSnapshotResponse(
                p.getId(), p.getCode(), p.getType(), p.getValue());
        }

        return new OrderResponse(
            response.id(),
            response.orderCode(),
            response.userId(),
            response.userFullName(),
            response.addressId(),
            response.addressLabel(),
            response.shippingRecipientSnapshot(),
            response.shippingPhoneSnapshot(),
            response.shippingAddressSnapshot(),
            response.shippingProviderNameSnapshot(),
            promo,
            response.subtotal(),
            response.discountAmount(),
            response.shippingFee(),
            response.totalAmount(),
            response.status(),
            response.note(),
            details,
            histories,
            response.createdAt(),
            response.updatedAt(),
            response.deliveryMethod(),
            response.deliveryDate(),
            response.deliverySlotId(),
            response.deliverySlotSnapshot(),
            response.buildingCodeSnapshot(),
            response.buildingNameSnapshot(),
            response.floorSnapshot(),
            response.apartmentNumberSnapshot(),
            response.recipientNameSnapshot(),
            response.recipientPhoneSnapshot(),
            response.deliveryNoteSnapshot()
        );
    }

    record PaymentItemSnapshot(Long productId, String productName, java.math.BigDecimal quantity, java.math.BigDecimal priceAtPayment) {}
}
