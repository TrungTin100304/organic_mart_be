package com.bryan.repository;

import com.bryan.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByPromotionIdAndOrderId(Long promotionId, Long orderId);

    Optional<PromotionUsage> findByPromotionIdAndUserId(Long promotionId, Long userId);

    long countByPromotionId(Long promotionId);

    long countByPromotionIdAndUserId(Long promotionId, Long userId);
}
