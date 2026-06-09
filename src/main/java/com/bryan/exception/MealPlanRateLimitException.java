package com.bryan.exception;

public class MealPlanRateLimitException extends RuntimeException {
    public MealPlanRateLimitException(String message) {
        super(message);
    }
}
