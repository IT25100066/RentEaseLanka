package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceRecord,Long> {


    List<MaintenanceRecord> findByVehicleAndOdometerAtService(Vehicle vehicle, Double odometerAtService);


    List<MaintenanceRecord> findByVehicle(Vehicle vehicle);

    List<MaintenanceRecord> findByVehicleAndStatus(Vehicle vehicle,MaintenanceStatus status);

    List<MaintenanceRecord> findByScheduledDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("select distinct m from MaintenanceRecord m left join fetch m.vehicle")
    List<MaintenanceRecord> findAllWithVehicle();

        @Query("""
                        SELECT COALESCE(SUM(m.cost), 0)
                        FROM MaintenanceRecord m
                        WHERE m.status = com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus.COMPLETED
                            AND m.cost IS NOT NULL
                            AND m.completionDate >= :from
                            AND m.completionDate < :to
                        """)
        BigDecimal getCompletedCost(@Param("from") LocalDateTime from,
                                                                @Param("to") LocalDateTime to);


        long countByStatus(MaintenanceStatus status);
}
