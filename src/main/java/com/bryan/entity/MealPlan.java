package com.bryan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plan")
@Getter
@Setter
@NoArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private LocalDate startDate;

    @Column(name = "number_of_days", nullable = false)
    private int numberOfDays = 3;

    @Column(name = "meals_per_day", nullable = false)
    private int mealsPerDay = 3;

    @Column(nullable = false)
    private int servings = 1;

    @Column(name = "diet_type", nullable = false)
    private String dietType = "NORMAL";

    @Column(name = "daily_calorie_target")
    private Integer dailyCalorieTarget;

    @Column(name = "budget_max", precision = 12, scale = 2)
    private BigDecimal budgetMax;

    @Column(name = "max_cooking_minutes")
    private Integer maxCookingMinutes;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object preferences;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealPlanStatus status = MealPlanStatus.GENERATING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_allergens", columnDefinition = "jsonb")
    private List<String> userAllergens;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "excluded_ingredients", columnDefinition = "jsonb")
    private List<String> excludedIngredients;

    @Column(name = "total_calories_per_day")
    private Integer totalCaloriesPerDay;

    @Column(name = "total_protein_per_day")
    private Integer totalProteinPerDay;

    @Column(name = "total_carbs_per_day")
    private Integer totalCarbsPerDay;

    @Column(name = "total_fat_per_day")
    private Integer totalFatPerDay;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC, mealNumber ASC")
    private List<Meal> meals = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addMeal(Meal meal) {
        meals.add(meal);
        meal.setMealPlan(this);
    }
}
