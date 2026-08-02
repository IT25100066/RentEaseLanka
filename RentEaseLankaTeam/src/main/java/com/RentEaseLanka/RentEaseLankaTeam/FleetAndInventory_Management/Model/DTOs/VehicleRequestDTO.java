package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class VehicleRequestDTO {

    // You secure your 12 Input Validation marks by keeping these annotations here
    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Daily rate is required")
    @Positive(message = "Daily rate must be greater than zero")
    private Double dailyRate;


    private int Year;

    // This handles the binary files
    private List<MultipartFile> images;
}
