package com.bryan.controller.admin;

import com.bryan.dto.response.AdminPaymentRequestResponse;
import com.bryan.dto.response.AdminSepayWebhookEventResponse;
import com.bryan.dto.response.ApiResponse;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.SepayWebhookEvent;
import com.bryan.repository.PaymentRequestRepository;
import com.bryan.repository.SepayWebhookEventRepository;
import com.bryan.repository.specification.AdminSearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payment-audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentAuditController {

    private final PaymentRequestRepository paymentRequestRepository;
    private final SepayWebhookEventRepository sepayWebhookEventRepository;

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<Page<AdminPaymentRequestResponse>>> getPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminPaymentRequestResponse> result = paymentRequestRepository
                .findAll(AdminSearchSpecifications.paymentRequests(status, search), pageable)
                .map(AdminPaymentRequestResponse::from);
        return ApiResponse.success(result);
    }

    @GetMapping("/webhooks")
    public ResponseEntity<ApiResponse<Page<AdminSepayWebhookEventResponse>>> getWebhooks(
            @RequestParam(required = false) SepayWebhookEvent.EventStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminSepayWebhookEventResponse> result = sepayWebhookEventRepository
                .findAll(AdminSearchSpecifications.webhookEvents(status, search), pageable)
                .map(AdminSepayWebhookEventResponse::from);
        return ApiResponse.success(result);
    }
}
