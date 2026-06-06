package com.bryan.service;

import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.request.VietQrWebhookRequest;
import com.bryan.dto.response.VietQrPaymentResponse;

public interface VietQrPaymentService {

    VietQrPaymentResponse createPayment(VietQrPaymentRequest request);

    VietQrPaymentResponse getPayment(Long id);

    VietQrPaymentResponse confirmPayment(VietQrWebhookRequest request, String webhookSecret);
}
