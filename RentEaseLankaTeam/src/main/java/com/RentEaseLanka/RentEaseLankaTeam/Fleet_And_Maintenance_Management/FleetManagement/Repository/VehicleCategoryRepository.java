package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory,Long> {
    Optional<VehicleCategory> findCategoryByName(String name);

    VehicleCategory getVehicleCategoriesById(@NotNull(message = "Category Id is required") Long categoryId);

     VehicleCategory findByName(String filter);
}
