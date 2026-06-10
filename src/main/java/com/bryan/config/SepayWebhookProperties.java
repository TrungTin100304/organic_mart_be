package com.bryan.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sepay")
@Getter
@Setter
public class SepayWebhookProperties {

    private boolean enabled;
    private String webhookApiKey;
    private String bankAccount;
    private String bankCode;

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }
        if (webhookApiKey == null || webhookApiKey.isBlank()) {
            throw new IllegalStateException("sepay.webhook-api-key configuration is required for webhook authentication");
        }
        if (bankAccount == null || bankAccount.isBlank()) {
            throw new IllegalStateException("sepay.bank-account configuration is required for payment matching");
        }
    }
}
