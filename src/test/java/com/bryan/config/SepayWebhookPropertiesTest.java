package com.bryan.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SepayWebhookPropertiesTest {

    @Test
    void disabledIntegrationDoesNotRequireWebhookCredentials() {
        SepayWebhookProperties properties = new SepayWebhookProperties();

        assertDoesNotThrow(properties::validate);
    }
}
