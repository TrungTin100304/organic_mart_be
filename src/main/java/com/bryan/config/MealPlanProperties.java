package com.bryan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "meal-plan")
@Getter
@Setter
public class MealPlanProperties {
    private int maxDays = 7;
    private int maxMealsPerDay = 4;
    private int maxGenerationsPerHour = 5;
}
