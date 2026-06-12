package com.bryan.service;

import com.bryan.dto.request.AiMealRequest;
import com.bryan.dto.request.AiMealResponse;

public interface GeminiAiService {
    AiMealResponse generateMealPlan(AiMealRequest request);
}
