package com.bryan.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vietqr")
@Getter
@Setter
public class VietQrProperties {

    private String bankId;
    private String bankCode;
    private String accountNo;
    private String accountName;
    private String template = "compact2";

    @PostConstruct
    public void validate() {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalStateException("vietqr.account-no configuration is required for QR generation");
        }
    }
}
