package com.bryan.repository;

import com.bryan.entity.ResidentialBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentialBuildingRepository extends JpaRepository<ResidentialBuilding, Long> {
    Optional<ResidentialBuilding> findByCode(String code);
    List<ResidentialBuilding> findByIsActiveTrueOrderByDisplayOrderAsc();
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
