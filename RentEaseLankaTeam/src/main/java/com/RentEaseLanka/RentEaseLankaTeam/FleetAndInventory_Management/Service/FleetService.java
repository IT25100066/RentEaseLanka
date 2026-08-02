package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import jakarta.validation.Valid;

public interface FleetService {

     void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest);

     VehicleCategory createCategory(@Valid VehicleCategoryCreateRequestDTO dto);
}
