package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateCategoryPriceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;

import java.util.List;

public interface VehicleCategoryService {
    VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto);

    List<CategoryResponseDTO> getAllCategories();

    boolean UpdateCategoryPrice(UpdateCategoryPriceDTO dto);

}
