package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponseDTO {

    private Long id;
    private String licensePlate;
    private String model;
    private int year;
    private double currentOdometer;
    private String status;
    private BigDecimal dailyRentalRate;

    // Category info (flattened to avoid circular references)
    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Image URLs the frontend can use directly
    private List<String> imageUrls;
}
