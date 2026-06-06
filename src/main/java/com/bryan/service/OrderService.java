package com.bryan.service;

import com.bryan.dto.request.CreateOrderRequest;
import com.bryan.dto.request.UpdateOrderStatusRequest;
import com.bryan.dto.response.OrderListResponse;
import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrderFromCart(CreateOrderRequest request);

    Page<OrderListResponse> getMyOrders(Pageable pageable);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByCode(String orderCode);

    Page<OrderListResponse> getAllOrders(Pageable pageable);

    Page<OrderListResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long orderId);
}
