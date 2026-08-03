package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.DTOs.*;

import java.util.List;

public interface FleetService {

     void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest);

     List<VehicleResponseDTO> getAllVehicles();

     VehicleResponseDTO getVehicleById(Long id);

     boolean updateVehicle(UpdateVehicleRequestDTO dto);

     boolean updateOdometer(UpdateOdometerDTO dto);

      boolean makeAvailable(List<Long> vehicleIdList);

      List<VehicleResponseDTO> pendingVehicles();
}
