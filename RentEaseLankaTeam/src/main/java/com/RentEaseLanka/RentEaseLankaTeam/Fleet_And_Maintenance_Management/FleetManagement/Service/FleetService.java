package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateOdometerDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateVehicleRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FleetService {

     void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest);

     List<VehicleResponseDTO> getAllVehicles();

     VehicleResponseDTO getVehicleById(Long id);

     boolean updateVehicle(UpdateVehicleRequestDTO dto);

     boolean updateOdometer(UpdateOdometerDTO dto);

      boolean makeAvailable(List<Long> vehicleIdList);

      List<VehicleResponseDTO> pendingVehicles();

    List<VehicleResponseDTO> getVehiclesByFilter(String filterType, @Nullable String category, @Nullable String status);

    boolean setVehicleToOutOfService(Long id);
}
