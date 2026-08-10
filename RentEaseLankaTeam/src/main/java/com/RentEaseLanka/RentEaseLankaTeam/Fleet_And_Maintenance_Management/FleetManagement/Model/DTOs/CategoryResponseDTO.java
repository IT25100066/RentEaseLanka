package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryResponseDTO {

    private String name;
    private Long id;
    private String description;
    private BigDecimal price;

}
