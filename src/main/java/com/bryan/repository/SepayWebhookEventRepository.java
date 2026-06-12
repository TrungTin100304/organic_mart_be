package com.bryan.repository;

import com.bryan.entity.SepayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SepayWebhookEventRepository extends JpaRepository<SepayWebhookEvent, Long>, JpaSpecificationExecutor<SepayWebhookEvent> {

    Optional<SepayWebhookEvent> findBySepayTransactionId(String sepayTransactionId);

    boolean existsBySepayTransactionId(String sepayTransactionId);
}
