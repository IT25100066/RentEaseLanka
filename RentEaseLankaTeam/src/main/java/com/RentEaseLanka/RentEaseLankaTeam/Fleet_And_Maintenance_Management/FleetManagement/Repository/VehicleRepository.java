package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByStatus(VehicleStatus status);

    @Query("select distinct v from Vehicle v left join fetch v.category")
    List<Vehicle> findAllWithCategory();

    void deleteById(Long vehicleId);

    long countByStatus(VehicleStatus available);

}
