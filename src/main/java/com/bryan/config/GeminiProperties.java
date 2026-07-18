package com.bryan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiProperties {
    private String apiKey;
    private String model = "gemini-2.5-flash";
    private int timeoutSeconds = 60;
    private int maxRetries = 1;
    private int maxOutputTokens = 32768;
    private int thinkingBudget = 0;
}
