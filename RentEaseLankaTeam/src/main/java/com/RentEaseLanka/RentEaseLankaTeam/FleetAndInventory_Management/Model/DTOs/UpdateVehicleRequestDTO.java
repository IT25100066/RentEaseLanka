package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVehicleRequestDTO {

    private Long id;
    private BigDecimal dailyRentalRate;
    private String model;
    private Long categoryId;
    private int year ;


}
