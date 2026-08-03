package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleCategoryServiceImpl implements VehicleCategoryService{

    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    @Override
    public VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto) {

        boolean isCategoryPresent = vehicleCategoryRepository.findCategoryByName(dto.getName()).isPresent();

        try{
            if (!isCategoryPresent){
                VehicleCategory category = new VehicleCategory();
                category.setBasePrice(dto.getBasePrice());
                category.setName(dto.getName());
                category.setDescription(dto.getDescription());
                vehicleCategoryRepository.save(category);
                return category;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        List<CategoryResponseDTO> allCategories = new ArrayList<>();
        List<VehicleCategory> vehicleCategories = vehicleCategoryRepository.findAll();

       for(VehicleCategory c : vehicleCategories){
           CategoryResponseDTO category = new CategoryResponseDTO();
           category.setId(c.getId());
           category.setName(c.getName());
           allCategories.add(category);
       }

       return allCategories;


    }
}
