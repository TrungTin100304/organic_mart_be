package com.bryan.dto.response;

import com.bryan.entity.Promotion;
import com.bryan.entity.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminPromotionResponse(
        Long id,
        String code,
        String name,
        String description,
        PromotionType type,
        BigDecimal value,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        LocalDate validFrom,
        LocalDate validTo,
        Integer usageLimit,
        Integer usageLimitPerUser,
        Integer timesUsed,
        boolean active,
        LocalDateTime createdAt
) {
    public static AdminPromotionResponse from(Promotion promotion) {
        return new AdminPromotionResponse(
                promotion.getId(), promotion.getCode(), promotion.getName(), promotion.getDescription(),
                promotion.getType(), promotion.getValue(), promotion.getMinOrderAmount(),
                promotion.getMaxDiscountAmount(), promotion.getValidFrom(), promotion.getValidTo(),
                promotion.getUsageLimit(), promotion.getUsageLimitPerUser(), promotion.getTimesUsed(),
                promotion.isActive(), promotion.getCreatedAt()
        );
    }
}
