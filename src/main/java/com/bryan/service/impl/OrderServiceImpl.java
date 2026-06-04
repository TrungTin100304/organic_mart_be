package com.bryan.service.impl;

import com.bryan.dto.request.CreateOrderRequest;
import com.bryan.dto.request.UpdateOrderStatusRequest;
import com.bryan.dto.response.OrderListResponse;
import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.OrderMapper;
import com.bryan.repository.*;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository batchRepository;
    private final PromotionRepository promotionRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + orderCode));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getMyOrders(Pageable pageable) {
        Long userId = getCurrentUserId();
        return orderRepository.findByUserId(userId, pageable)
            .map(OrderListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllOrders(pageable)
            .map(OrderListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
            .map(OrderListResponse::from);
    }

    @Override
    public OrderResponse createOrderFromCart(CreateOrderRequest request) {
        User user = getAuthenticatedUserEntity();
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        UserAddress address = userAddressRepository.findById(request.addressId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + request.addressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to the current user");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal shippingFee = new BigDecimal("30000.00");
        BigDecimal discountAmount = BigDecimal.ZERO;
        Promotion promotion = null;

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal price = cartItem.getProduct().getPrice();
            BigDecimal itemSubtotal = price.multiply(cartItem.getQuantity());
            subtotal = subtotal.add(itemSubtotal);
        }

        if (request.promotionCode() != null && !request.promotionCode().isBlank()) {
             promotion = promotionRepository.findByCode(request.promotionCode())
                .filter(Promotion::isActive)
                .filter(p -> {
                    LocalDate now = LocalDate.now();
                    return !now.isBefore(p.getValidFrom()) && !now.isAfter(p.getValidTo());
                })
                .orElseThrow(() -> new BadRequestException("Invalid or expired promotion code"));

            if (promotion.getMinOrderAmount() != null &&
                subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
                throw new BadRequestException("Order amount does not meet minimum requirement for this promotion");
            }

            discountAmount = calculateDiscount(promotion, subtotal);
        }

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPromotion(promotion);
        order.setOrderCode(generateOrderCode());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setNote(request.note());
        order.setShippingRecipientSnapshot(address.getRecipientName());
        order.setShippingPhoneSnapshot(address.getRecipientPhone());
        order.setShippingAddressSnapshot(buildFullAddress(address));

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal priceAtPurchase = product.getPrice();

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPriceAtPurchase(priceAtPurchase);

            batchRepository.findFirstByProductIdOrderByExpiryDateAsc(product.getId())
                .filter(b -> b.getQuantityRemaining().compareTo(cartItem.getQuantity()) >= 0)
                .ifPresent(b -> {
                    b.setQuantityRemaining(b.getQuantityRemaining().subtract(cartItem.getQuantity()));
                    batchRepository.save(b);
                    detail.setBatch(b);
                });

            order.addDetail(detail);
        }

        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setFromStatus(null);
        initialHistory.setToStatus(OrderStatus.PENDING);
        initialHistory.setChangedBy(user);
        initialHistory.setNote("Order placed by customer");
        order.addStatusHistory(initialHistory);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order created: {} for user {}", savedOrder.getOrderCode(), user.getEmail());
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrderById(orderId);
        OrderStatus fromStatus = order.getStatus();
        OrderStatus toStatus = request.status();

        validateStatusTransition(fromStatus, toStatus);

        User admin = getAuthenticatedUserEntity();
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(admin);
        history.setNote(request.note());
        order.addStatusHistory(history);

        order.setStatus(toStatus);
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} status changed: {} -> {} by {}",
            savedOrder.getOrderCode(), fromStatus, toStatus, admin.getEmail());
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        Long userId = getCurrentUserId();

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
            order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Only PENDING or CONFIRMED orders can be cancelled");
        }

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getBatch() != null && detail.getQuantity() != null) {
                InventoryBatch batch = detail.getBatch();
                batch.setQuantityRemaining(batch.getQuantityRemaining().add(detail.getQuantity()));
                batchRepository.save(batch);
            }
        }

        OrderStatus fromStatus = order.getStatus();
        OrderStatusHistory history = new OrderStatusHistory();
        history.setFromStatus(fromStatus);
        history.setToStatus(OrderStatus.CANCELLED);
        history.setChangedBy(order.getUser());
        history.setNote("Cancelled by customer");
        order.addStatusHistory(history);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} cancelled by user {}", savedOrder.getOrderCode(), userId);
        return mapToResponse(savedOrder);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private OrderResponse mapToResponse(Order order) {
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
            response.updatedAt()
        );
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal discount;
        if (promotion.getType() == PromotionType.PERCENTAGE) {
            discount = subtotal.multiply(promotion.getValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = promotion.getValue();
        }
        if (promotion.getMaxDiscountAmount() != null &&
            discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }
        return discount;
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private String buildFullAddress(UserAddress address) {
        StringBuilder sb = new StringBuilder();
        if (address.getFullAddress() != null) sb.append(address.getFullAddress());
        if (address.getWard() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getWard());
        }
        if (address.getDistrict() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getDistrict());
        }
        if (address.getCity() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCity());
        }
        return sb.toString();
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        boolean valid = switch (to) {
            case PENDING -> false;
            case CONFIRMED -> from == OrderStatus.PENDING;
            case PROCESSING -> from == OrderStatus.CONFIRMED;
            case SHIPPED -> from == OrderStatus.PROCESSING;
            case DELIVERED -> from == OrderStatus.SHIPPED;
            case CANCELLED -> from == OrderStatus.PENDING || from == OrderStatus.CONFIRMED;
            case REFUNDED -> from == OrderStatus.DELIVERED || from == OrderStatus.CANCELLED;
        };
        if (!valid) {
            throw new BadRequestException(
                "Cannot change status from " + from + " to " + to);
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails ud)) {
            throw new BadRequestException("Authentication required");
        }
        return ud.getId();
    }

    private User getAuthenticatedUserEntity() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
