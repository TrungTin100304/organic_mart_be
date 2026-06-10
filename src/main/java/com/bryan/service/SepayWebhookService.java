package com.bryan.service;

import com.bryan.dto.request.SepayWebhookRequest;
import com.bryan.dto.response.SepayWebhookResponse;

public interface SepayWebhookService {

    /**
     * Process an inbound SePay webhook notification.
     * Returns a raw JSON success/failure response suitable for SePay to receive.
     *
     * @param request   the deserialized webhook payload from SePay
     * @param apiKey    the Authorization header value after "Apikey " prefix
     * @return         always returns HTTP 200 with {success:true} for valid auth;
     *                  callers should escalate internal errors to 5xx
     */
    SepayWebhookResponse handleWebhook(SepayWebhookRequest request, String apiKey);
}
