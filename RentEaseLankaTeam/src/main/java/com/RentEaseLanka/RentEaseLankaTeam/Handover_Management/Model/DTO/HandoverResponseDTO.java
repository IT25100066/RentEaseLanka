package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandoverResponseDTO {

    private Long id;

    // Flattened vehicle details
    private Long vehicleId;
    private String vehicleLicensePlate;
    private String vehicleModel;
    private int vehicleYear;
    private String vehicleStatus;
    private double vehicleCurrentOdometer;

    // Decoupled foreign references
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private String staffId;

    // Handover details
    private String status;

    private Double startingMileage;
    private boolean hasDamage;
    private boolean paymentVerified;
    private String damageDescription;
    private String fuelLevel;
    private String notes;

    // Timestamps
    private LocalDateTime handoverDateTime;
    private LocalDateTime completionDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
