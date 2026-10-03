package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByCustomerId(Long customerId);
}