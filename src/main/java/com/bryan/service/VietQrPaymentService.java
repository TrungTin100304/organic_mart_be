package com.bryan.service;

import com.bryan.dto.request.VietQrPaymentRequest;
import com.bryan.dto.response.OrderResponse;
import com.bryan.dto.response.VietQrPaymentResponse;

public interface VietQrPaymentService {

    VietQrPaymentResponse createPayment(VietQrPaymentRequest request);

    VietQrPaymentResponse getPayment(Long id);

    /**
     * Complete an order from a PAID VietQR payment.
     * Idempotent: returns the existing order if payment already has one.
     *
     * @param paymentId the payment request ID
     * @return the created (or existing) order
     */
    OrderResponse completeOrderFromPayment(Long paymentId);

    /**
     * Complete an order after a trusted payment confirmation such as a SePay webhook.
     * This path does not depend on a browser-authenticated user session.
     */
    OrderResponse completeOrderFromConfirmedPayment(Long paymentId);
}
