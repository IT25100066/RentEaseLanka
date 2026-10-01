package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VehicleCreateRequestDTO {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Daily rate is required")
    @Positive(message = "Daily rate must be greater than zero")
    private BigDecimal dailyRate;

    private int year;

    @NotNull
    @Positive
    private double currentOdometer;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private List<MultipartFile> images;
}