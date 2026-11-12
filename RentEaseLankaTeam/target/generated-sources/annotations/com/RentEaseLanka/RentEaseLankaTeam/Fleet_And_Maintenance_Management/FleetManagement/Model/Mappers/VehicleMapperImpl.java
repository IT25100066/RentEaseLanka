package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-12T12:43:48+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.5 (Eclipse Adoptium)"
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
        vehicleResponseDTO.createdAt( vehicle.getCreatedAt() );
        vehicleResponseDTO.currentOdometer( vehicle.getCurrentOdometer() );
        vehicleResponseDTO.dailyRentalRate( vehicle.getDailyRentalRate() );
        vehicleResponseDTO.id( vehicle.getId() );
        vehicleResponseDTO.licensePlate( vehicle.getLicensePlate() );
        vehicleResponseDTO.model( vehicle.getModel() );
        if ( vehicle.getStatus() != null ) {
            vehicleResponseDTO.status( vehicle.getStatus().name() );
        }
        vehicleResponseDTO.updatedAt( vehicle.getUpdatedAt() );
        vehicleResponseDTO.year( vehicle.getYear() );

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
