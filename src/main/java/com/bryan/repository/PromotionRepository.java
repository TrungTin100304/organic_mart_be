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

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Promotion> findAllByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Promotion p WHERE p.code = :code")
    java.util.Optional<Promotion> findByCodeForUpdate(@org.springframework.data.repository.query.Param("code") String code);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Promotion p WHERE p.id = :id")
    java.util.Optional<Promotion> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
