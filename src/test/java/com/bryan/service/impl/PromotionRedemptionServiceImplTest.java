package com.bryan.service.impl;

import com.bryan.entity.Promotion;
import com.bryan.entity.PromotionType;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.PromotionRepository;
import com.bryan.repository.PromotionUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionRedemptionServiceImplTest {

    @Mock PromotionRepository promotionRepository;
    @Mock PromotionUsageRepository promotionUsageRepository;
    @InjectMocks PromotionRedemptionServiceImpl service;

    @Test
    void reservesPromotionWhileHoldingLockedPromotionRow() {
        Promotion promotion = promotion();
        User user = new User();
        user.setId(7L);
        when(promotionRepository.findByCodeForUpdate("SAVE10")).thenReturn(Optional.of(promotion));
        when(promotionUsageRepository.countByUserIdAndPromotionId(7L, 1L)).thenReturn(0);

        var applied = service.reserve(" save10 ", user, new BigDecimal("200000"));

        assertEquals(new BigDecimal("20000.00"), applied.discountAmount());
        assertEquals(1, promotion.getTimesUsed());
        verify(promotionRepository).save(promotion);
    }

    @Test
    void rejectsPromotionAtUsageLimit() {
        Promotion promotion = promotion();
        promotion.setTimesUsed(10);
        promotion.setUsageLimit(10);
        User user = new User();
        user.setId(7L);
        when(promotionRepository.findByCodeForUpdate("SAVE10")).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> service.reserve("SAVE10", user, new BigDecimal("200000")));
    }

    private Promotion promotion() {
        Promotion promotion = new Promotion();
        promotion.setId(1L);
        promotion.setCode("SAVE10");
        promotion.setType(PromotionType.PERCENTAGE);
        promotion.setValue(new BigDecimal("10"));
        promotion.setValidFrom(LocalDate.now().minusDays(1));
        promotion.setValidTo(LocalDate.now().plusDays(1));
        promotion.setActive(true);
        promotion.setTimesUsed(0);
        promotion.setUsageLimit(10);
        promotion.setUsageLimitPerUser(1);
        return promotion;
    }
}
