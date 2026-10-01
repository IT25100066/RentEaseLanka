package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateCategoryPriceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers.VehicleCategoryMapper;

@Slf4j
@Service
public class VehicleCategoryServiceImpl implements VehicleCategoryService{

    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    @Autowired
    private VehicleCategoryMapper vehicleCategoryMapper;

    @Override
    public VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto) {

        boolean isCategoryPresent = vehicleCategoryRepository.findCategoryByName(dto.getName()).isPresent();

        try{
            if (!isCategoryPresent){
                VehicleCategory category = new VehicleCategory();
                category.setBasePrice(dto.getBasePrice());
                category.setName(dto.getName().toLowerCase());
                category.setDescription(dto.getDescription());
                vehicleCategoryRepository.save(category);
                log.info(category.getName()+" Vehicle Category Created");
                return category;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<VehicleCategory> vehicleCategories = vehicleCategoryRepository.findAll();

        return vehicleCategories.stream()
                .map(vehicleCategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean UpdateCategoryPrice(UpdateCategoryPriceDTO dto) {
        VehicleCategory c = vehicleCategoryRepository.getVehicleCategoriesById(dto.getId());
        if (c != null){
            c.setBasePrice(dto.getBasePrice());
            vehicleCategoryRepository.save(c);
            log.info( c.getName()+ "Category Price Updated ");
            return true;
        }
        log.error("Category Could not Found");
        return false;
    }
}
