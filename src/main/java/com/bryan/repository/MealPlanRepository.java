package com.bryan.repository;

import com.bryan.entity.MealPlan;
import com.bryan.entity.MealPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    List<MealPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<MealPlan> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT mp FROM MealPlan mp LEFT JOIN FETCH mp.meals WHERE mp.id = :id AND mp.user.id = :userId")
    Optional<MealPlan> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(mp) FROM MealPlan mp WHERE mp.user.id = :userId AND mp.status IN :statuses")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<MealPlanStatus> statuses);

    void deleteByIdAndUserId(Long id, Long userId);
}
