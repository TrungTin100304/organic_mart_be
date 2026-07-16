package com.bryan.service.impl;

import com.bryan.config.GeminiProperties;
import com.bryan.dto.request.AiMealRequest;
import com.bryan.exception.AiResponseParseException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiAiServiceImplJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void joinsAllGeminiTextPartsBeforeParsingJson() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse(
                        "STOP",
                        List.of("{\"days\":[", validDayJson() + "]}")
                )));

        var response = service(restTemplate).generateMealPlan(request());

        assertEquals(1, response.days.size());
        assertEquals("Bữa sáng rau củ", response.days.getFirst().meals.getFirst().name);
    }

    @Test
    @SuppressWarnings("unchecked")
    void requestsStructuredJsonWithEnoughOutputTokensAndThinkingDisabled() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse(
                        "STOP",
                        List.of("{\"days\":[" + validDayJson() + "]}")
                )));

        service(restTemplate).generateMealPlan(request());

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class));

        Map<String, Object> generationConfig =
                (Map<String, Object>) entityCaptor.getValue().getBody().get("generationConfig");

        assertEquals("application/json", generationConfig.get("responseMimeType"));
        assertEquals(32768, generationConfig.get("maxOutputTokens"));
        assertInstanceOf(Map.class, generationConfig.get("responseSchema"));
        assertEquals(Map.of("thinkingBudget", 0), generationConfig.get("thinkingConfig"));
    }

    @Test
    void reportsTokenTruncationWithoutLeakingJsonParserDetails() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse("MAX_TOKENS", List.of("{\"days\":["))));

        AiResponseParseException exception = assertThrows(
                AiResponseParseException.class,
                () -> service(restTemplate).generateMealPlan(request())
        );

        assertEquals("Phản hồi từ Gemini bị cắt do vượt giới hạn nội dung.", exception.getMessage());
        assertTrue(exception.getMessage().contains("Gemini"));
    }

    @Test
    void doesNotExposeJsonParserDetailsForMalformedStructuredOutput() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse("STOP", List.of("{\"days\":["))));

        AiResponseParseException exception = assertThrows(
                AiResponseParseException.class,
                () -> service(restTemplate).generateMealPlan(request())
        );

        assertEquals("Gemini trả về dữ liệu không hợp lệ hoặc chưa hoàn chỉnh.", exception.getMessage());
    }

    private GeminiAiServiceImpl service(RestTemplate restTemplate) {
        GeminiProperties properties = new GeminiProperties();
        properties.setApiKey("test-key");
        properties.setMaxRetries(0);
        return new GeminiAiServiceImpl(properties, restTemplate, objectMapper);
    }

    private AiMealRequest request() {
        return new AiMealRequest(
                1, 1, 1, "NORMAL", null, null, null,
                List.of(), List.of(), null, List.of()
        );
    }

    private String geminiResponse(String finishReason, List<String> textParts) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "candidates", List.of(Map.of(
                        "finishReason", finishReason,
                        "content", Map.of(
                                "parts", textParts.stream().map(text -> Map.of("text", text)).toList()
                        )
                ))
        ));
    }

    private String validDayJson() {
        return """
                {
                  "dayNumber": 1,
                  "meals": [{
                    "mealType": "BREAKFAST",
                    "name": "Bữa sáng rau củ",
                    "description": "Món ăn nhẹ",
                    "ingredients": ["rau củ"],
                    "cookingInstructions": "Nấu chín",
                    "preparationMinutes": 10,
                    "cookingMinutes": 15,
                    "calories": 300,
                    "proteinGrams": 10,
                    "carbsGrams": 40,
                    "fatGrams": 8
                  }]
                }
                """;
    }
}
