package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {

    List<VehicleImage> findByVehicleId(Long vehicleId);
}
