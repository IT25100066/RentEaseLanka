package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;

import java.util.List;

public interface VehicleCategoryService {
    VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto);

    List<CategoryResponseDTO> getAllCategories();
}
