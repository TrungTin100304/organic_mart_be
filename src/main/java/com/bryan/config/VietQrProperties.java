package com.bryan.config;

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
    private String accountNo;
    private String accountName;
    private String template = "compact2";
    private String webhookSecret;
}
