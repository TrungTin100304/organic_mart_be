package com.bryan.service;

import com.bryan.config.MealPlanProperties;
import com.bryan.exception.MealPlanRateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MealPlanRateLimitService {

    private final MealPlanProperties mealPlanProperties;

    private final Map<Long, RateLimitEntry> userRequestCounts = new ConcurrentHashMap<>();

    public void checkRateLimit(Long userId) {
        RateLimitEntry entry = userRequestCounts.compute(userId, (id, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RateLimitEntry();
            }
            existing.increment();
            return existing;
        });

        if (entry.count > mealPlanProperties.getMaxGenerationsPerHour()) {
            throw new MealPlanRateLimitException(
                    "Bạn đã tạo quá nhiều thực đơn trong 1 giờ. Vui lòng thử lại sau."
            );
        }
    }

    private static class RateLimitEntry {
        int count = 1;
        final LocalDateTime createdAt = LocalDateTime.now();
        final LocalDateTime resetsAt = LocalDateTime.now().plusHours(1);

        void increment() { count++; }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(resetsAt);
        }
    }
}
