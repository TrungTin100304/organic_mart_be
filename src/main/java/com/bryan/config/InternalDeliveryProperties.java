package com.bryan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "internal-delivery")
@Getter
@Setter
public class InternalDeliveryProperties {

    private BigDecimal standardFee = BigDecimal.ZERO;
    private BigDecimal expressFee = new BigDecimal("20000");
    private BigDecimal scheduledFee = BigDecimal.ZERO;
    private int expressMinutes = 30;
    private int standardMinutes = 60;
}
