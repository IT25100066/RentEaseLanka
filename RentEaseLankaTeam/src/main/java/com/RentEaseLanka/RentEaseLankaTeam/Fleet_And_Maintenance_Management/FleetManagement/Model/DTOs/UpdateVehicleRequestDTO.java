package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs;

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
