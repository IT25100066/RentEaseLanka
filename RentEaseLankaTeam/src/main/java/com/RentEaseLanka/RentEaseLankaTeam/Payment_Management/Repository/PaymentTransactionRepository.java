package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentPurpose;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentTransaction;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentTransaction> findFirstByBookingIdAndPurposeAndStatusOrderByCreatedAtDesc(
            Long bookingId, PaymentPurpose purpose, PaymentTransactionStatus status);
    List<PaymentTransaction> findByBookingIdAndPurposeAndStatus(
            Long bookingId, PaymentPurpose purpose, PaymentTransactionStatus status);
}
