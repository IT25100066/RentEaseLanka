package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FleetService {

     Vehicle createVehicleWithImages(VehicleRequestDTO vehicleCreateRequest);
}
