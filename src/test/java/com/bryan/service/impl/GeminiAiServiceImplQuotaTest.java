package com.bryan.service.impl;

import com.bryan.config.GeminiProperties;
import com.bryan.dto.request.AiMealRequest;
import com.bryan.exception.AiQuotaExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiAiServiceImplQuotaTest {

    @Test
    void shouldReturnFriendlyQuotaErrorWithoutLeakingGeminiResponse() {
        GeminiProperties properties = new GeminiProperties();
        properties.setApiKey("test-key");
        properties.setModel("gemini-2.0-flash");
        properties.setMaxRetries(0);

        RestTemplate restTemplate = mock(RestTemplate.class);
        HttpClientErrorException quotaError = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                null,
                "{\"error\":{\"message\":\"Quota exceeded for metric: free_tier_requests\"}}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(quotaError);

        GeminiAiServiceImpl service = new GeminiAiServiceImpl(properties, restTemplate, new ObjectMapper());
        AiMealRequest request = new AiMealRequest(
                1, 3, 1, "NORMAL", null, null, null,
                List.of(), List.of(), null, List.of()
        );

        AiQuotaExceededException exception = assertThrows(
                AiQuotaExceededException.class,
                () -> service.generateMealPlan(request)
        );

        assertEquals(
                "Dịch vụ tạo thực đơn AI đã hết hạn mức sử dụng. Vui lòng thử lại sau hoặc liên hệ quản trị viên.",
                exception.getMessage()
        );
        verify(restTemplate).exchange(
                startsWith("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }
}
