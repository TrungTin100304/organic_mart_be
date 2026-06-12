package com.bryan.service.impl;

import com.bryan.config.GeminiProperties;
import com.bryan.dto.request.AiDayPlan;
import com.bryan.dto.request.AiMeal;
import com.bryan.dto.request.AiMealRequest;
import com.bryan.dto.request.AiMealResponse;
import com.bryan.exception.AiQuotaExceededException;
import com.bryan.exception.AiResponseParseException;
import com.bryan.exception.AiTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiServiceImpl implements com.bryan.service.GeminiAiService {

    private final GeminiProperties geminiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String DEFAULT_GEMINI_MODEL = "gemini-2.5-flash";
    private static final Set<String> RETIRED_GEMINI_MODELS = Set.of(
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash-lite-001"
    );
    private static final Map<String, Object> MEAL_PLAN_RESPONSE_SCHEMA = mealPlanResponseSchema();

    @Override
    public AiMealResponse generateMealPlan(AiMealRequest request) {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            throw new AiTimeoutException("Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.");
        }

        String configuredModel = geminiProperties.getModel();
        String model = configuredModel == null
                || configuredModel.isBlank()
                || RETIRED_GEMINI_MODELS.contains(configuredModel)
                ? DEFAULT_GEMINI_MODEL
                : configuredModel;
        if (!model.equals(configuredModel)) {
            log.warn("Configured Gemini model '{}' is unavailable; using '{}'.", configuredModel, model);
        }
        String url = String.format(GEMINI_API_URL, model) + "?key=" + geminiProperties.getApiKey();

        String prompt = buildPrompt(request);

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.9);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", geminiProperties.getMaxOutputTokens());
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", MEAL_PLAN_RESPONSE_SCHEMA);
        generationConfig.put("thinkingConfig", Map.of("thinkingBudget", geminiProperties.getThinkingBudget()));

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int retries = geminiProperties.getMaxRetries();
        AiTimeoutException lastException = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                String rawResponse = callGemini(url, entity);
                return parseResponse(rawResponse);
            } catch (ResourceAccessException ex) {
                lastException = new AiTimeoutException("Gemini API request timed out after " + geminiProperties.getTimeoutSeconds() + "s");
                log.warn("Gemini API attempt {}/{} timeout: {}", attempt + 1, retries + 1, ex.getMessage());
            } catch (AiResponseParseException ex) {
                log.warn("Gemini API attempt {}/{} parse error: {}", attempt + 1, retries + 1, ex.getMessage());
                if (attempt == retries) throw ex;
            } catch (HttpClientErrorException ex) {
                log.error("Gemini API client error: {}", ex.getMessage());
                if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    throw new AiQuotaExceededException(
                            "Dịch vụ tạo thực đơn AI đã hết hạn mức sử dụng. Vui lòng thử lại sau hoặc liên hệ quản trị viên."
                    );
                }
                throw new AiTimeoutException("Gemini API rejected the request: " + ex.getMessage());
            } catch (HttpServerErrorException ex) {
                log.error("Gemini API server error: {}", ex.getMessage());
                throw new AiTimeoutException("Gemini AI service is temporarily unavailable. Please try again later.");
            }
        }

        throw lastException != null ? lastException : new AiTimeoutException("Gemini AI request failed after all retries.");
    }

    private String callGemini(String url, HttpEntity<Map<String, Object>> entity) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (ResourceAccessException ex) {
            throw ex;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to call Gemini API: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private AiMealResponse parseResponse(String rawResponse) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(rawResponse, Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new AiResponseParseException("No response from Gemini AI.");
            }

            Map<String, Object> candidate = candidates.get(0);
            if ("MAX_TOKENS".equals(candidate.get("finishReason"))) {
                throw new AiResponseParseException("Phản hồi từ Gemini bị cắt do vượt giới hạn nội dung.");
            }

            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            if (content == null) {
                throw new AiResponseParseException("Gemini không trả về nội dung thực đơn.");
            }
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new AiResponseParseException("Empty response from Gemini AI.");
            }

            String text = parts.stream()
                    .map(part -> part.get("text"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.joining());
            if (text == null || text.isBlank()) {
                throw new AiResponseParseException("No text content in Gemini response.");
            }

            // Strip markdown code fences if present
            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            } else if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            // Parse the JSON text
            Map<String, Object> mealPlanMap = objectMapper.readValue(text, Map.class);
            List<Map<String, Object>> daysList = (List<Map<String, Object>>) mealPlanMap.get("days");

            if (daysList == null) {
                throw new AiResponseParseException("AI response missing 'days' field.");
            }

            AiMealResponse response = new AiMealResponse();
            response.days = daysList.stream().map(dayMap -> {
                AiDayPlan day = new AiDayPlan();
                day.dayNumber = ((Number) dayMap.get("dayNumber")).intValue();
                List<Map<String, Object>> mealsList = (List<Map<String, Object>>) dayMap.get("meals");
                day.meals = mealsList.stream().map(mealMap -> {
                    AiMeal meal = new AiMeal();
                    meal.mealType = (String) mealMap.get("mealType");
                    meal.name = (String) mealMap.get("name");
                    meal.description = (String) mealMap.get("description");
                    meal.ingredients = (List<String>) mealMap.get("ingredients");
                    meal.cookingInstructions = (String) mealMap.get("cookingInstructions");
                    meal.preparationMinutes = mealMap.get("preparationMinutes") != null ? ((Number) mealMap.get("preparationMinutes")).intValue() : null;
                    meal.cookingMinutes = mealMap.get("cookingMinutes") != null ? ((Number) mealMap.get("cookingMinutes")).intValue() : null;
                    meal.calories = mealMap.get("calories") != null ? ((Number) mealMap.get("calories")).intValue() : 0;
                    meal.proteinGrams = mealMap.get("proteinGrams") != null ? ((Number) mealMap.get("proteinGrams")).doubleValue() : 0.0;
                    meal.carbsGrams = mealMap.get("carbsGrams") != null ? ((Number) mealMap.get("carbsGrams")).doubleValue() : 0.0;
                    meal.fatGrams = mealMap.get("fatGrams") != null ? ((Number) mealMap.get("fatGrams")).doubleValue() : 0.0;
                    return meal;
                }).toList();
                return day;
            }).toList();

            return response;
        } catch (AiResponseParseException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Unable to parse Gemini meal-plan response: {}", ex.getMessage());
            throw new AiResponseParseException("Gemini trả về dữ liệu không hợp lệ hoặc chưa hoàn chỉnh.", ex);
        }
    }

    private static Map<String, Object> mealPlanResponseSchema() {
        Map<String, Object> mealSchema = Map.of(
                "type", "object",
                "properties", Map.ofEntries(
                        Map.entry("mealType", Map.of(
                                "type", "string",
                                "enum", List.of("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                        )),
                        Map.entry("name", Map.of("type", "string")),
                        Map.entry("description", Map.of("type", "string")),
                        Map.entry("ingredients", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        )),
                        Map.entry("cookingInstructions", Map.of("type", "string")),
                        Map.entry("preparationMinutes", Map.of("type", "integer")),
                        Map.entry("cookingMinutes", Map.of("type", "integer")),
                        Map.entry("calories", Map.of("type", "integer")),
                        Map.entry("proteinGrams", Map.of("type", "number")),
                        Map.entry("carbsGrams", Map.of("type", "number")),
                        Map.entry("fatGrams", Map.of("type", "number"))
                ),
                "required", List.of(
                        "mealType", "name", "description", "ingredients", "cookingInstructions",
                        "preparationMinutes", "cookingMinutes", "calories",
                        "proteinGrams", "carbsGrams", "fatGrams"
                )
        );

        Map<String, Object> daySchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "dayNumber", Map.of("type", "integer"),
                        "meals", Map.of("type", "array", "items", mealSchema)
                ),
                "required", List.of("dayNumber", "meals")
        );

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "days", Map.of("type", "array", "items", daySchema)
                ),
                "required", List.of("days")
        );
    }

    private String buildPrompt(AiMealRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là chuyên gia dinh dưỡng của Organic Mart. ");
        sb.append("Hãy lập kế hoạch ăn uống cho ").append(request.numberOfDays()).append(" ngày, ");
        sb.append("mỗi ngày ").append(request.mealsPerDay()).append(" bữa, ");
        sb.append("mỗi bữa ").append(request.servings()).append(" khẩu phần.\n\n");

        sb.append("Chế độ ăn: ").append(request.dietType()).append("\n");

        if (request.dailyCalorieTarget() != null) {
            sb.append("Mục tiêu calo/ngày: ").append(request.dailyCalorieTarget()).append(" kcal\n");
        }
        if (request.budgetMax() != null) {
            sb.append("Ngân sách tối đa: ").append(request.budgetMax()).append(" VNĐ/ngày\n");
        }
        if (request.maxCookingMinutes() != null) {
            sb.append("Thời gian nấu tối đa mỗi món: ").append(request.maxCookingMinutes()).append(" phút\n");
        }
        if (request.preferredIngredients() != null && !request.preferredIngredients().isEmpty()) {
            sb.append("Nguyên liệu yêu thích: ").append(String.join(", ", request.preferredIngredients())).append("\n");
        }
        if (request.excludedIngredients() != null && !request.excludedIngredients().isEmpty()) {
            sb.append("Không sử dụng: ").append(String.join(", ", request.excludedIngredients())).append("\n");
        }
        if (request.userAllergenNames() != null && !request.userAllergenNames().isEmpty()) {
            sb.append("Người dùng bị dị ứng với: ").append(String.join(", ", request.userAllergenNames())).append(" - TUYỆT ĐỐI KHÔNG dùng các thành phần này!\n");
        }
        if (request.additionalNotes() != null && !request.additionalNotes().isBlank()) {
            sb.append("Ghi chú: ").append(request.additionalNotes()).append("\n");
        }

        sb.append("\nHãy trả về JSON theo format sau, CHỈ trả về JSON thuần (không có markdown code fences, không có giải thích):\n");
        sb.append("""
            {
              "days": [
                {
                  "dayNumber": 1,
                  "meals": [
                    {
                      "mealType": "BREAKFAST|LUNCH|DINNER|SNACK",
                      "name": "Tên món ăn",
                      "description": "Mô tả ngắn món ăn",
                      "ingredients": ["nguyên liệu 1", "nguyên liệu 2"],
                      "cookingInstructions": "Các bước nấu...",
                      "preparationMinutes": 15,
                      "cookingMinutes": 30,
                      "calories": 450,
                      "proteinGrams": 30.5,
                      "carbsGrams": 45.0,
                      "fatGrams": 15.0
                    }
                  ]
                }
              ]
            }
            """);

        sb.append("\nYêu cầu:\n");
        sb.append("- Trả về đúng ").append(request.numberOfDays()).append(" ngày.\n");
        sb.append("- Mỗi ngày có đúng ").append(request.mealsPerDay()).append(" bữa.\n");
        sb.append("- Sử dụng các loại bữa: ");
        String[] mealTypes = new String[]{"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
        for (int i = 0; i < request.mealsPerDay(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(mealTypes[i % 4]);
        }
        sb.append(".\n");
        sb.append("- Ingredients chỉ ghi TÊN NGUYÊN LIỆU, không có số lượng.\n");
        sb.append("- calories, proteinGrams, carbsGrams, fatGrams là giá trị ước tính.\n");
        sb.append("- Không dùng các nguyên liệu gây dị ứng.\n");
        sb.append("- Nếu chế độ ăn là VEGETARIAN hoặc VEGAN, không dùng thịt, cá.\n");
        sb.append("- Trả về JSON thuần, không có gì khác.\n");

        return sb.toString();
    }
}
