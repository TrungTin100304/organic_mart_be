package com.bryan.controller;

import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.request.VietQrWebhookRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.VietQrPaymentResponse;
import com.bryan.service.VietQrPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/vietqr")
@RequiredArgsConstructor
@Tag(name = "VietQR Payments", description = "Create and inspect VietQR payment requests")
public class VietQrPaymentController {

    private final VietQrPaymentService vietQrPaymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a VietQR payment request from the current cart")
    public ResponseEntity<ApiResponse<VietQrPaymentResponse>> createPayment(
        @Valid @RequestBody VietQrPaymentRequest request
    ) {
        return ApiResponse.success(201, vietQrPaymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get one of the current user's VietQR payment requests")
    public ResponseEntity<ApiResponse<VietQrPaymentResponse>> getPayment(@PathVariable Long id) {
        return ApiResponse.success(vietQrPaymentService.getPayment(id));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Confirm a VietQR payment from a trusted transaction notifier")
    public ResponseEntity<ApiResponse<VietQrPaymentResponse>> confirmPayment(
        @Valid @RequestBody VietQrWebhookRequest request,
        @RequestHeader("X-Webhook-Secret") String webhookSecret
    ) {
        return ApiResponse.success(vietQrPaymentService.confirmPayment(request, webhookSecret));
    }
}
