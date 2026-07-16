package com.bryan.repository;

import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.DeliverySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliverySettingRepository extends JpaRepository<DeliverySetting, Long> {
    Optional<DeliverySetting> findByDeliveryType(DeliveryMethod deliveryType);
    List<DeliverySetting> findAllByOrderByDisplayOrderAsc();
}
