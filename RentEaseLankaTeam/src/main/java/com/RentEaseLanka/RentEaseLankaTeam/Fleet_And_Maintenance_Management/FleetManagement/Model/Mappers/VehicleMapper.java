package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.description", target = "categoryDescription")
    @Mapping(target = "imageUrls", expression = "java(mapImagesToUrls(vehicle.getImages()))")
    VehicleResponseDTO toDto(Vehicle vehicle);

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<VehicleImage> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(VehicleImage::getFilePath)
                .collect(Collectors.toList());
    }
}
