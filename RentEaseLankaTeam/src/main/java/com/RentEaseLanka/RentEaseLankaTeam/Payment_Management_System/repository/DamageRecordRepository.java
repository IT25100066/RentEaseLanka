package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.DamageRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageRecordRepository extends JpaRepository<DamageRecord, Long> {
    List<DamageRecord> findByInvoiceId(Long invoiceId);
}
