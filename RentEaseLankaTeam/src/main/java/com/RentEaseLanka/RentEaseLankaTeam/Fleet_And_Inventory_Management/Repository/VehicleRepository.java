package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByStatus(VehicleStatus status);

}
