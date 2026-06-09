package com.bryan.repository;

import com.bryan.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query("SELECT m FROM Meal m WHERE m.mealPlan.id = :mealPlanId ORDER BY m.dayNumber ASC, m.mealNumber ASC")
    List<Meal> findByMealPlanIdOrderByDayNumberAscMealNumberAsc(@Param("mealPlanId") Long mealPlanId);

    @Query("SELECT m FROM Meal m JOIN m.mealPlan mp WHERE m.id = :mealId AND mp.user.id = :userId")
    Optional<Meal> findByIdAndMealPlanUserId(@Param("mealId") Long mealId, @Param("userId") Long userId);

    @Query("SELECT m FROM Meal m LEFT JOIN FETCH m.products WHERE m.mealPlan.id = :mealPlanId ORDER BY m.dayNumber ASC, m.mealNumber ASC")
    List<Meal> findByMealPlanIdWithProducts(@Param("mealPlanId") Long mealPlanId);

    void deleteByIdAndMealPlanUserId(Long id, Long userId);
}
