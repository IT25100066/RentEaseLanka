package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HandoverRepository extends JpaRepository<HandoverRecord, Long> {

    List<HandoverRecord> findByVehicle(Vehicle vehicle);

    List<HandoverRecord> findByVehicleId(Long vehicleId);

    List<HandoverRecord> findByStatus(HandoverStatus status);

    boolean existsByVehicleAndStatusIn(Vehicle vehicle, Collection<HandoverStatus> statuses);

    boolean existsByBookingIdAndStatusIn(Long bookingId, Collection<HandoverStatus> statuses);

    List<HandoverRecord> findByBookingId(Long bookingId);
}
