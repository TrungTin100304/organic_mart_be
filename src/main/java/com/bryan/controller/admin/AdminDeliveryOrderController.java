package com.bryan.controller.admin;

import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.DeliveryOrderResponse;
import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import com.bryan.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/delivery-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeliveryOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeliveryOrderResponse>>> getDeliveryOrders(
            @RequestParam(required = false) java.time.LocalDate deliveryDate,
            @RequestParam(required = false) String buildingCode,
            @RequestParam(required = false) Long deliverySlotId,
            @RequestParam(required = false) DeliveryMethod deliveryMethod,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        Page<Order> orders;

        if (deliveryDate != null) {
            orders = orderRepository.findByDeliveryDate(deliveryDate, pageable);
        } else if (buildingCode != null) {
            orders = orderRepository.findByBuildingCode(buildingCode, pageable);
        } else if (deliverySlotId != null) {
            orders = orderRepository.findByDeliverySlotId(deliverySlotId, pageable);
        } else if (deliveryMethod != null) {
            orders = orderRepository.findByDeliveryMethod(deliveryMethod, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAllOrders(pageable);
        }

        Page<DeliveryOrderResponse> page = orders.map(DeliveryOrderResponse::from);
        return ApiResponse.success(page);
    }
}
