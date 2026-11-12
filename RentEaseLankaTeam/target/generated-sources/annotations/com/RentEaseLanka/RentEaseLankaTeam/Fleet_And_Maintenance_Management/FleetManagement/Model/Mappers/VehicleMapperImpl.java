package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-12T10:52:38+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 26.0.1 (Oracle Corporation)"
)
@Component
public class VehicleMapperImpl implements VehicleMapper {

    @Override
    public VehicleResponseDTO toDto(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }

        VehicleResponseDTO.VehicleResponseDTOBuilder vehicleResponseDTO = VehicleResponseDTO.builder();

        vehicleResponseDTO.categoryId( vehicleCategoryId( vehicle ) );
        vehicleResponseDTO.categoryName( vehicleCategoryName( vehicle ) );
        vehicleResponseDTO.categoryDescription( vehicleCategoryDescription( vehicle ) );
        vehicleResponseDTO.id( vehicle.getId() );
        vehicleResponseDTO.licensePlate( vehicle.getLicensePlate() );
        vehicleResponseDTO.model( vehicle.getModel() );
        vehicleResponseDTO.year( vehicle.getYear() );
        vehicleResponseDTO.currentOdometer( vehicle.getCurrentOdometer() );
        if ( vehicle.getStatus() != null ) {
            vehicleResponseDTO.status( vehicle.getStatus().name() );
        }
        vehicleResponseDTO.dailyRentalRate( vehicle.getDailyRentalRate() );
        vehicleResponseDTO.createdAt( vehicle.getCreatedAt() );
        vehicleResponseDTO.updatedAt( vehicle.getUpdatedAt() );

        vehicleResponseDTO.imageUrls( mapImagesToUrls(vehicle.getImages()) );

        return vehicleResponseDTO.build();
    }

    private Long vehicleCategoryId(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }
        VehicleCategory category = vehicle.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String vehicleCategoryName(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }
        VehicleCategory category = vehicle.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String vehicleCategoryDescription(Vehicle vehicle) {
        if ( vehicle == null ) {
            return null;
        }
        VehicleCategory category = vehicle.getCategory();
        if ( category == null ) {
            return null;
        }
        String description = category.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }
}
