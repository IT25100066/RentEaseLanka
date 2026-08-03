package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.DTOs.UpdateCategoryPriceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Inventory_Management.Model.VehicleCategory;

import java.util.List;

public interface VehicleCategoryService {
    VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto);

    List<CategoryResponseDTO> getAllCategories();

    boolean UpdateCategoryPrice(UpdateCategoryPriceDTO dto);
}
