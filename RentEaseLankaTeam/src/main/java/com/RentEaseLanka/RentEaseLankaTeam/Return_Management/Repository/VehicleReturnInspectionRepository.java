package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Model.VehicleReturnInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleReturnInspectionRepository extends JpaRepository<VehicleReturnInspection, Long> {
    Optional<VehicleReturnInspection> findByBookingId(Long bookingId);
}
