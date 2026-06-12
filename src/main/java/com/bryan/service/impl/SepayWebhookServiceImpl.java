package com.bryan.service.impl;

import com.bryan.config.SepayWebhookProperties;
import com.bryan.dto.request.SepayWebhookRequest;
import com.bryan.dto.response.SepayWebhookResponse;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.SepayWebhookEvent;
import com.bryan.entity.SepayWebhookEvent.EventStatus;
import com.bryan.repository.PaymentRequestRepository;
import com.bryan.repository.SepayWebhookEventRepository;
import com.bryan.service.SepayWebhookService;
import com.bryan.service.VietQrPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookServiceImpl implements SepayWebhookService {

    private static final String TRANSFER_TYPE_IN = "in";
    private static final Pattern TRANSFER_CODE_PATTERN = Pattern.compile("OM[A-Z0-9]{12}");

    private final SepayWebhookEventRepository webhookEventRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final SepayWebhookProperties properties;
    private final VietQrPaymentService vietQrPaymentService;

    @Override
    @Transactional
    public SepayWebhookResponse handleWebhook(SepayWebhookRequest request, String apiKey) {
        if (!isValidApiKey(apiKey)) {
            return SepayWebhookResponse.error("Unauthorized");
        }

        String txnId = String.valueOf(request.id());

        // ── Idempotency: already processed this SePay transaction? ──
        Optional<SepayWebhookEvent> existingEvent = webhookEventRepository
            .findBySepayTransactionId(txnId);
        if (existingEvent.isPresent()
            && existingEvent.get().getStatus() != EventStatus.REJECTED) {
            log.info("Sepay webhook idempotent hit: txnId={}, status={}",
                txnId, existingEvent.get().getStatus());
            return SepayWebhookResponse.ok();
        }

        // ── Save event as RECEIVED first (prevents concurrent processing) ──
        SepayWebhookEvent event = existingEvent.orElseGet(SepayWebhookEvent::new);
        event.setSepayTransactionId(txnId);
        event.setTransferAmount(request.transferAmount());
        event.setAccountNumber(request.accountNumber());
        event.setTransferType(request.transferType());
        event.setGateway(request.gateway());
        event.setStatus(EventStatus.RECEIVED);
        event.setRejectionReason(null);
        event.setProcessedAt(null);
        event = webhookEventRepository.save(event);

        // ── Only process credit (transfer in) ──
        if (!TRANSFER_TYPE_IN.equalsIgnoreCase(request.transferType())) {
            log.info("Sepay webhook ignored non-credit: txnId={}, type={}",
                txnId, request.transferType());
            markRejected(event, "transfer_type_not_in");
            return SepayWebhookResponse.ok();
        }

        // ── Validate account number ──
        String configuredAccount = properties.getBankAccount();
        if (configuredAccount == null
            || !configuredAccount.equals(request.accountNumber())) {
            log.warn("Sepay webhook account mismatch: txnId={}, receivedAccount={}",
                txnId, request.accountNumber());
            markRejected(event, "account_mismatch");
            return SepayWebhookResponse.ok();
        }

        // ── Resolve transfer code ──
        String transferCode = resolveTransferCode(request);
        if (transferCode == null) {
            log.warn("Sepay webhook: could not extract transfer code from txnId={}", txnId);
            markRejected(event, "code_not_found");
            return SepayWebhookResponse.ok();
        }

        transferCode = transferCode.toUpperCase();
        event.setTransferCode(transferCode);

        // ── Lookup payment ──
        Optional<PaymentRequest> optPayment = paymentRequestRepository.findByTransferCode(transferCode);
        if (optPayment.isEmpty()) {
            log.warn("Sepay webhook: no payment found for code={}, txnId={}", transferCode, txnId);
            markRejected(event, "payment_not_found");
            return SepayWebhookResponse.ok();
        }

        PaymentRequest payment = optPayment.get();
        event.setTransferCode(transferCode);

        // ── Already paid? ──
        if (payment.getStatus() == PaymentStatus.PAID) {
            if (txnId.equals(payment.getTransactionId())) {
                // Same transaction — idempotent OK
                log.info("Sepay webhook: txnId={} already recorded as paid for code={}",
                    txnId, transferCode);
            } else {
                log.warn("Sepay webhook: payment already paid, code={}, txnId={}, existingTxn={}",
                    transferCode, txnId, payment.getTransactionId());
            }
            vietQrPaymentService.completeOrderFromConfirmedPayment(payment.getId());
            markProcessed(event);
            return SepayWebhookResponse.ok();
        }

        // ── Expired? ──
        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Sepay webhook: payment expired, code={}, txnId={}", transferCode, txnId);
            markRejected(event, "payment_expired");
            return SepayWebhookResponse.ok();
        }

        // ── Cancelled? ──
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            log.warn("Sepay webhook: payment cancelled, code={}, txnId={}", transferCode, txnId);
            markRejected(event, "payment_cancelled");
            return SepayWebhookResponse.ok();
        }

        // ── Amount must match exactly ──
        if (payment.getAmount().compareTo(request.transferAmount()) != 0) {
            log.warn("Sepay webhook amount mismatch: code={}, txnId={}, expected={}, received={}",
                transferCode, txnId, payment.getAmount(), request.transferAmount());
            markRejected(event, "amount_mismatch");
            return SepayWebhookResponse.ok();
        }

        // ── DB-level idempotency: transaction already recorded? ──
        if (paymentRequestRepository.existsByTransactionId(txnId)) {
            log.warn("Sepay webhook DB idempotency hit: txnId={}, code={}", txnId, transferCode);
            markProcessed(event);
            return SepayWebhookResponse.ok();
        }

        // ── All checks passed — mark payment as PAID ──
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(txnId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRequestRepository.save(payment);
        vietQrPaymentService.completeOrderFromConfirmedPayment(payment.getId());

        markProcessed(event);
        log.info("Sepay webhook confirmed payment: code={}, txnId={}, amount={}",
            transferCode, txnId, request.transferAmount());

        return SepayWebhookResponse.ok();
    }

    private boolean isValidApiKey(String provided) {
        String configured = properties.getWebhookApiKey();
        if (configured == null || provided == null) {
            return false;
        }
        // Constant-time comparison
        return MessageDigest.isEqual(
            configured.getBytes(StandardCharsets.UTF_8),
            provided.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String resolveTransferCode(SepayWebhookRequest request) {
        // Priority 1: code field
        if (request.code() != null && !request.code().isBlank()) {
            return request.code().trim().toUpperCase();
        }
        // Priority 2: referenceCode field
        if (request.referenceCode() != null && !request.referenceCode().isBlank()) {
            String transferCode = extractCodeFromText(request.referenceCode().trim());
            if (transferCode != null) {
                return transferCode;
            }
        }
        // Priority 3: content field
        if (request.content() != null && !request.content().isBlank()) {
            return extractCodeFromText(request.content());
        }
        return null;
    }

    private String extractCodeFromText(String text) {
        Matcher m = TRANSFER_CODE_PATTERN.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private void markProcessed(SepayWebhookEvent event) {
        event.setStatus(EventStatus.PROCESSED);
        event.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(event);
    }

    private void markRejected(SepayWebhookEvent event, String reason) {
        event.setStatus(EventStatus.REJECTED);
        event.setRejectionReason(reason);
        event.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(event);
    }
}
