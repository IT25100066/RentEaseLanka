package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionLogRepository extends JpaRepository<PaymentTransactionLog, Long> {
    boolean existsByPayloadSnapshotContaining(String idempotencyKey);
}

