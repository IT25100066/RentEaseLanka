package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-12T10:52:38+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 26.0.1 (Oracle Corporation)"
)
@Component
public class VehicleCategoryMapperImpl implements VehicleCategoryMapper {

    @Override
    public CategoryResponseDTO toDto(VehicleCategory category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();

        categoryResponseDTO.setId( category.getId() );
        categoryResponseDTO.setName( category.getName() );
        categoryResponseDTO.setDescription( category.getDescription() );
        categoryResponseDTO.setPrice( category.getBasePrice() );

        return categoryResponseDTO;
    }
}
