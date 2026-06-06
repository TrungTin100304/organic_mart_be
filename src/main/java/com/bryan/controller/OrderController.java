package com.bryan.controller;

import com.bryan.dto.request.CreateOrderRequest;
import com.bryan.dto.request.UpdateOrderStatusRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.OrderListResponse;
import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.OrderStatus;
import com.bryan.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create an order from the current user's cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrderFromCart(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), response, "Order created successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's order history (paginated)")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderListResponse> orders = orderService.getMyOrders(pageable);
        return ApiResponse.success(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by ID (user sees own, admin sees all)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderById(id));
    }

    @GetMapping("/code/{orderCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by order code")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(@PathVariable String orderCode) {
        return ApiResponse.success(orderService.getOrderByCode(orderCode));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (admin only, paginated)")
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderListResponse> orders = (status != null)
            ? orderService.getOrdersByStatus(status, pageable)
            : orderService.getAllOrders(pageable);
        return ApiResponse.success(orders);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(orderService.updateOrderStatus(id, request),
            "Order status updated successfully");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel an order (customer cancels own order)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.cancelOrder(id), "Order cancelled successfully");
    }
}
