package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.*;
import org.jspecify.annotations.Nullable;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FleetService {

  void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest);

  List<String> addVehicleImages(Long vehicleId, List<MultipartFile> images);

  List<VehicleResponseDTO> getAllVehicles();

  VehicleResponseDTO getVehicleById(Long id);

  boolean updateVehicle(UpdateVehicleRequestDTO dto);

  boolean updateOdometer(UpdateOdometerDTO dto);

  boolean makeAvailable(List<Long> vehicleIdList);

  List<VehicleResponseDTO> pendingVehicles();

  List<VehicleResponseDTO> getVehiclesByFilter(String filterType, @Nullable String category, @Nullable String status);

  boolean setVehicleToOutOfService(Long id);

  VehicleRemoveResponseDTO removePendingVehicles(Long id);

  boolean deleteVehicle(Long id);
}
