package com.bryan.controller;

import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.OrderResponse;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.service.VietQrPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/vietqr")
@RequiredArgsConstructor
@Tag(name = "VietQR Payments", description = "Create and manage VietQR payment requests")
public class VietQrPaymentController {

    private final VietQrPaymentService vietQrPaymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a VietQR payment request from the current cart")
    public ResponseEntity<ApiResponse<VietQrPaymentResponse>> createPayment(
        @Valid @RequestBody VietQrPaymentRequest request
    ) {
        return ApiResponse.success(HttpStatus.CREATED.value(), vietQrPaymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get one of the current user's VietQR payment requests")
    public ResponseEntity<ApiResponse<VietQrPaymentResponse>> getPayment(@PathVariable Long id) {
        return ApiResponse.success(vietQrPaymentService.getPayment(id));
    }

    /**
     * Complete order from a paid VietQR payment.
     * Idempotent: returns the existing order if already created.
     */
    @PostMapping("/{paymentId}/complete-order")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create an order from a paid VietQR payment (idempotent)")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable Long paymentId) {
        return ApiResponse.success(vietQrPaymentService.completeOrderFromPayment(paymentId));
    }
}
