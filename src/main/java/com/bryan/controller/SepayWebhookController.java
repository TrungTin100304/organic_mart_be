package com.bryan.controller;

import com.bryan.dto.request.SepayWebhookRequest;
import com.bryan.dto.response.SepayWebhookResponse;
import com.bryan.service.SepayWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sepay")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "sepay", name = "enabled", havingValue = "true")
public class SepayWebhookController {

    private static final String API_KEY_PREFIX = "Apikey ";
    private final SepayWebhookService sepayWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<SepayWebhookResponse> handleWebhook(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody SepayWebhookRequest request
    ) {
        String apiKey = extractApiKey(authHeader);
        if (apiKey == null) {
            log.warn("Sepay webhook rejected: missing or malformed Authorization header");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(SepayWebhookResponse.error("Unauthorized"));
        }

        log.info("Sepay webhook received: txnId={}, amount={}, code={}",
            request.id(), request.transferAmount(), request.code());

        SepayWebhookResponse response = sepayWebhookService.handleWebhook(request, apiKey);
        return ResponseEntity.ok(response);
    }

    private String extractApiKey(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(API_KEY_PREFIX)) {
            return null;
        }
        String key = authHeader.substring(API_KEY_PREFIX.length());
        if (key.isBlank()) {
            return null;
        }
        return key;
    }
}
