package com.bryan.controller;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminPaymentAuditSecurityTest {

    @Test
    void paymentAuditControllerShouldBeAdminOnlyAndDtoShouldNotExposeRawPayload() throws Exception {
        Class<?> controller = Class.forName("com.bryan.controller.admin.AdminPaymentAuditController");
        PreAuthorize annotation = controller.getAnnotation(PreAuthorize.class);
        assertEquals("hasRole('ADMIN')", annotation.value());

        Class<?> webhookDto = Class.forName("com.bryan.dto.response.AdminSepayWebhookEventResponse");
        assertFalse(Arrays.stream(webhookDto.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch("rawPayload"::equals));
    }
}
