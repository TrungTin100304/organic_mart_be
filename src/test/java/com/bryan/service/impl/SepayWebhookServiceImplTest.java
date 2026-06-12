package com.bryan.service.impl;

import com.bryan.config.SepayWebhookProperties;
import com.bryan.dto.request.SepayWebhookRequest;
import com.bryan.dto.response.SepayWebhookResponse;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.SepayWebhookEvent;
import com.bryan.repository.PaymentRequestRepository;
import com.bryan.repository.SepayWebhookEventRepository;
import com.bryan.service.VietQrPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SepayWebhookServiceImplTest {

    private static final String API_KEY = "sepay-webhook-secret";
    private static final String ACCOUNT = "00003981468";
    // Transfer codes are 14 chars: OM + 12 alphanumeric
    private static final String CODE = "OMABC123XYZDEF";
    private static final String CODE2 = "OMDEF456GHILMN";
    private static final String CODE3 = "OMGHI789JKLMN";

    @Mock private SepayWebhookEventRepository evRepo;
    @Mock private PaymentRequestRepository payRepo;
    @Mock private SepayWebhookProperties props;
    @Mock private VietQrPaymentService vietQrPaymentService;

    @InjectMocks private SepayWebhookServiceImpl svc;

    @BeforeEach
    void init() {
        when(props.getWebhookApiKey()).thenReturn(API_KEY);
        when(props.getBankAccount()).thenReturn(ACCOUNT);
    }

    private void stubSave() {
        when(evRepo.save(any(SepayWebhookEvent.class))).thenAnswer(i -> {
            SepayWebhookEvent e = i.getArgument(0);
            if (e.getId() == null) {
                // Simulate DB assigning an ID
                try {
                    java.lang.reflect.Field f = SepayWebhookEvent.class.getDeclaredField("id");
                    f.setAccessible(true);
                    f.set(e, System.nanoTime());
                } catch (Exception ex) { /* ignore */ }
            }
            return e;
        });
    }

    // ── API Key ──────────────────────────────────────────────────────

    @Nested
    class Auth {

        @Test @DisplayName("valid Apikey <key> → accepted")
        void acceptValid() {
            SepayWebhookRequest r = make(1L, CODE, "in");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
        }

        @Test @DisplayName("null → 401")
        void rejectNull() {
            SepayWebhookRequest r = make(2L, CODE, "in");
            SepayWebhookResponse resp = svc.handleWebhook(r, null);
            assertFalse(resp.success());
            assertEquals("Unauthorized", resp.message());
        }

        @Test @DisplayName("wrong prefix → 401")
        void rejectWrongPrefix() {
            SepayWebhookRequest r = make(3L, CODE, "in");
            assertFalse(svc.handleWebhook(r, "Bearer token").success());
        }

        @Test @DisplayName("Apikey + wrong key → 401")
        void rejectWrongKey() {
            SepayWebhookRequest r = make(4L, CODE, "in");
            assertFalse(svc.handleWebhook(r, "Apikey bad-key").success());
        }

        @Test @DisplayName("empty → 401")
        void rejectEmpty() {
            SepayWebhookRequest r = make(5L, CODE, "in");
            assertFalse(svc.handleWebhook(r, "").success());
        }
    }

    // ── Transfer type ───────────────────────────────────────────────

    @Nested
    class TransferType {

        @Test @DisplayName("type=in → processed (saves event)")
        void acceptIn() {
            SepayWebhookRequest r = make(10L, CODE, "in");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(evRepo, atLeastOnce()).save(any(SepayWebhookEvent.class));
        }

        @Test @DisplayName("type=out → ignored, no payment lookup")
        void ignoreOut() {
            SepayWebhookRequest r = make(11L, CODE, "out");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo, never()).findByTransferCode(any());
        }
    }

    // ── Account number ──────────────────────────────────────────────

    @Nested
    class AccountNumber {

        @Test @DisplayName("wrong account → ignored, no payment lookup")
        void wrongAccount() {
            SepayWebhookRequest r = new SepayWebhookRequest(
                12L, "sepay", LocalDateTime.now(),
                "WRONGACCOUNT", null, CODE, "content", "in",
                "desc", new BigDecimal("100000"), BigDecimal.ZERO, null);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo, never()).findByTransferCode(any());
        }
    }

    // ── Transfer code resolution ──────────────────────────────────────

    @Nested
    class TransferCodeResolution {

        @Test @DisplayName("code field → used directly (exact 14-char code)")
        void codeField() {
            SepayWebhookRequest r = make(20L, CODE, "in");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(CODE)).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo).findByTransferCode(CODE);
        }

        @Test @DisplayName("code=null → extracted from referenceCode field (exact 14-char code)")
        void referenceCodeField() {
            // code field is null; referenceCode field contains the exact 14-char code
            SepayWebhookRequest r = new SepayWebhookRequest(
                21L, "sepay", LocalDateTime.now(),
                ACCOUNT, null, null, "payment description", "in",
                "desc", new BigDecimal("100000"), BigDecimal.ZERO, CODE2);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(CODE2)).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo).findByTransferCode(CODE2);
        }

        @Test @DisplayName("code field takes priority over referenceCode")
        void priority() {
            // code field set → used even if referenceCode also set
            SepayWebhookRequest r = new SepayWebhookRequest(
                22L, "sepay", LocalDateTime.now(),
                ACCOUNT, null, CODE, "desc", "in",
                "desc", new BigDecimal("100000"), BigDecimal.ZERO, CODE2);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(CODE)).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo).findByTransferCode(CODE);
            verify(payRepo, never()).findByTransferCode(CODE2);
        }

        @Test @DisplayName("no OM code anywhere → returns success (SePay stops retry)")
        void noCodeFound() {
            SepayWebhookRequest r = new SepayWebhookRequest(
                23L, "sepay", LocalDateTime.now(),
                ACCOUNT, null, null, "generic payment no code", "in",
                "desc", new BigDecimal("100000"), BigDecimal.ZERO, null);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo, never()).findByTransferCode(any());
        }
    }

    // ── Payment validation ─────────────────────────────────────────

    @Nested
    class PaymentProcessing {

        @Test @DisplayName("correct amount, pending → PAID")
        void marksPaid() {
            BigDecimal amt = new BigDecimal("220000");
            PaymentRequest pay = makePayment(CODE, amt);
            SepayWebhookRequest r = makeWithAmt(30L, CODE, "in", amt);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(CODE)).thenReturn(Optional.of(pay));
            when(payRepo.existsByTransactionId(any())).thenReturn(false);
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            assertEquals(PaymentStatus.PAID, pay.getStatus());
            assertEquals("30", pay.getTransactionId());
            assertNotNull(pay.getPaidAt());
            verify(vietQrPaymentService).completeOrderFromConfirmedPayment(pay.getId());
        }

        @Test
        @DisplayName("already paid without order -> completes order idempotently")
        void completesOrderForAlreadyPaidPayment() {
            BigDecimal amt = new BigDecimal("220000");
            PaymentRequest pay = makePayment(CODE, amt);
            pay.setStatus(PaymentStatus.PAID);
            pay.setTransactionId("33");
            SepayWebhookRequest r = makeWithAmt(33L, CODE, "in", amt);
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(CODE)).thenReturn(Optional.of(pay));

            assertTrue(svc.handleWebhook(r, API_KEY).success());

            verify(vietQrPaymentService).completeOrderFromConfirmedPayment(pay.getId());
        }

        @Test @DisplayName("wrong amount → NOT paid")
        void wrongAmount() {
            String code = CODE;
            PaymentRequest pay = makePayment(code, new BigDecimal("220000"));
            SepayWebhookRequest r = makeWithAmt(31L, code, "in", new BigDecimal("99000"));
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(code)).thenReturn(Optional.of(pay));
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            assertEquals(PaymentStatus.PENDING, pay.getStatus());
            assertNull(pay.getTransactionId());
        }

        @Test @DisplayName("expired payment → NOT paid")
        void expired() {
            String code = CODE;
            PaymentRequest pay = makePayment(code, new BigDecimal("220000"));
            pay.setExpiresAt(LocalDateTime.now().minusMinutes(5));
            SepayWebhookRequest r = make(32L, code, "in");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            when(payRepo.findByTransferCode(code)).thenReturn(Optional.of(pay));
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            assertEquals(PaymentStatus.PENDING, pay.getStatus());
        }
    }

    // ── Idempotency ───────────────────────────────────────────────

    @Nested
    class Idempotency {

        @Test @DisplayName("duplicate SePay txnId → idempotent (processed event exists)")
        void duplicateTxnId() {
            SepayWebhookEvent existing = new SepayWebhookEvent();
            existing.setSepayTransactionId("40");
            existing.setStatus(SepayWebhookEvent.EventStatus.PROCESSED);
            SepayWebhookRequest r = make(40L, CODE, "in");
            when(evRepo.findBySepayTransactionId("40")).thenReturn(Optional.of(existing));
            stubSave();
            assertTrue(svc.handleWebhook(r, API_KEY).success());
            verify(payRepo, never()).save(any());
        }

        @Test @DisplayName("event saved for every received request")
        void eventSaved() {
            SepayWebhookRequest r = make(41L, CODE, "out");
            stubSave();
            when(evRepo.findBySepayTransactionId(any())).thenReturn(Optional.empty());
            svc.handleWebhook(r, API_KEY);
            verify(evRepo, atLeastOnce()).save(any(SepayWebhookEvent.class));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private SepayWebhookRequest make(Long id, String code, String type) {
        return makeWithAmt(id, code, type, new BigDecimal("100000"));
    }

    private SepayWebhookRequest makeWithAmt(Long id, String code, String type, BigDecimal amt) {
        return new SepayWebhookRequest(
            id, "sepay", LocalDateTime.now(),
            ACCOUNT, null, code, "content", type,
            "desc", amt, BigDecimal.ZERO, null);
    }

    private PaymentRequest makePayment(String code, BigDecimal amt) {
        com.bryan.entity.User u = new com.bryan.entity.User();
        u.setId(1L);
        com.bryan.entity.UserAddress a = new com.bryan.entity.UserAddress();
        a.setId(5L);
        a.setUser(u);
        PaymentRequest p = new PaymentRequest();
        p.setId(10L);
        p.setUser(u);
        p.setAddress(a);
        p.setAmount(amt);
        p.setTransferCode(code);
        p.setStatus(PaymentStatus.PENDING);
        p.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        return p;
    }
}
