package com.bryan.controller;

import com.bryan.dto.request.AddTrackingRequest;
import com.bryan.dto.request.CalculateShippingRateRequest;
import com.bryan.dto.request.CreateShipmentRequest;
import com.bryan.dto.request.UpdateShipmentRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ShipmentListResponse;
import com.bryan.dto.response.ShipmentResponse;
import com.bryan.dto.response.ShippingRateResponse;
import com.bryan.entity.ShipmentStatus;
import com.bryan.service.ShipmentService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Shipment and tracking management")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/rate")
    @Operation(summary = "Calculate shipping rate for a provider (public)")
    public ResponseEntity<ApiResponse<ShippingRateResponse>> calculateRate(
            @Valid @ModelAttribute CalculateShippingRateRequest request) {
        return ApiResponse.success(shipmentService.calculateRate(request));
    }

    @GetMapping("/rates")
    @Operation(summary = "Calculate shipping rates for all active providers (public)")
    public ResponseEntity<ApiResponse<List<ShippingRateResponse>>> calculateAllRates(
            @RequestParam String province,
            @RequestParam String district,
            @RequestParam(required = false) String ward,
            @RequestParam(defaultValue = "1") BigDecimal weightKg) {
        return ApiResponse.success(shipmentService.calculateAllRates(province, district, ward, weightKg));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new shipment for an order (admin only)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse response = shipmentService.createShipment(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), response, "Shipment created successfully");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all shipments (admin only, paginated)")
    public ResponseEntity<ApiResponse<Page<ShipmentListResponse>>> getAllShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ShipmentStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShipmentListResponse> shipments = (status != null)
            ? shipmentService.getShipmentsByStatus(status, pageable)
            : shipmentService.getAllShipments(pageable);
        return ApiResponse.success(shipments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shipment by ID")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentById(@PathVariable Long id) {
        return ApiResponse.success(shipmentService.getShipmentById(id));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByOrderId(@PathVariable Long orderId) {
        return ApiResponse.success(shipmentService.getShipmentByOrderId(orderId));
    }

    @GetMapping("/tracking/{trackingCode}")
    @Operation(summary = "Get shipment by tracking code (public)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByTrackingCode(
            @PathVariable String trackingCode) {
        return ApiResponse.success(shipmentService.getShipmentByTrackingCode(trackingCode));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a shipment (admin only)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return ApiResponse.success(shipmentService.updateShipment(id, request),
            "Shipment updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a shipment (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ApiResponse.success(null, "Shipment deleted successfully");
    }

    @PostMapping("/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a tracking event to a shipment (admin only)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> addTracking(
            @PathVariable Long id,
            @Valid @RequestBody AddTrackingRequest request) {
        return ApiResponse.success(shipmentService.addTracking(id, request),
            "Tracking added successfully");
    }
}
