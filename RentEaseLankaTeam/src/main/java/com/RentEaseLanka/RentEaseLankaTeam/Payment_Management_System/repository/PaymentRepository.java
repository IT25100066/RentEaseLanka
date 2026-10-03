package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.Payment;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);
    Optional<Payment> findFirstByInvoiceIdAndPaymentTypeAndStatus(Long invoiceId, PaymentType paymentType, PaymentStatus status);
}