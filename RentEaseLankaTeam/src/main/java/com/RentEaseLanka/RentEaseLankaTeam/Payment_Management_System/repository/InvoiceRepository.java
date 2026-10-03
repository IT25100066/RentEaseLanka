package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT i FROM Invoice i WHERE i.status IN (com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus.UNPAID, com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus.PARTIALLY_PAID) AND i.pickupDeadline < :now")
    List<Invoice> findOverduePickups(@Param("now") LocalDateTime now);

    Optional<Invoice> findByVehicleReservationId(Long vehicleReservationId);

    List<Invoice> findByCustomerId(Long customerId);
}

