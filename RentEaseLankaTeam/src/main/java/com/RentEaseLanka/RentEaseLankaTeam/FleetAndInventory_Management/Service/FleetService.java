package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.*;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import jakarta.validation.Valid;

import java.util.List;

public interface FleetService {

     void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest);

     List<VehicleResponseDTO> getAllVehicles();

     VehicleResponseDTO getVehicleById(Long id);

     boolean updateVehicle(UpdateVehicleRequestDTO dto);

     boolean updateOdometer(UpdateOdometerDTO dto);
}
