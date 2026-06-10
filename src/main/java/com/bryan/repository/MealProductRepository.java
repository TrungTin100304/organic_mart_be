package com.bryan.repository;

import com.bryan.entity.MealProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealProductRepository extends JpaRepository<MealProduct, Long> {

    List<MealProduct> findByMealId(Long mealId);

    @Query("SELECT mp FROM MealProduct mp WHERE mp.meal.id IN :mealIds")
    List<MealProduct> findByMealIdIn(@Param("mealIds") List<Long> mealIds);

    @Query("SELECT mp FROM MealProduct mp JOIN mp.meal m WHERE m.mealPlan.id = :mealPlanId")
    List<MealProduct> findByMealPlanId(@Param("mealPlanId") Long mealPlanId);

    @Query("SELECT mp FROM MealProduct mp JOIN mp.meal m WHERE m.mealPlan.id = :mealPlanId AND mp.product IS NOT NULL AND mp.isInStock = true AND mp.addedToCart = false")
    List<MealProduct> findAvailableForCart(@Param("mealPlanId") Long mealPlanId);
}
