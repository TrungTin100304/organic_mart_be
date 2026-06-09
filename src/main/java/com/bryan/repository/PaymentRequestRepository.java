package com.bryan.repository;

import com.bryan.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long>, JpaSpecificationExecutor<PaymentRequest> {

    Optional<PaymentRequest> findByIdAndUserId(Long id, Long userId);

    Optional<PaymentRequest> findByTransferCode(String transferCode);

    boolean existsByTransactionId(String transactionId);
}
