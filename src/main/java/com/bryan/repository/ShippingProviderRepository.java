package com.bryan.repository;

import com.bryan.entity.ShippingProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {

    List<ShippingProvider> findByIsActiveTrue();

    Optional<ShippingProvider> findByName(String name);
}
