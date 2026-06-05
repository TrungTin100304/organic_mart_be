package com.bryan.repository;

import com.bryan.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    List<Promotion> findByIsActiveTrue();

    List<Promotion> findByIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            LocalDate validDate1, LocalDate validDate2);

    List<Promotion> findByIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualAndUsageLimitGreaterThan(
            LocalDate validDate1, LocalDate validDate2, Integer timesUsed);

    boolean existsByCode(String code);
}
