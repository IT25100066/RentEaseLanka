package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePerformanceDTO {

    private Long vehicleId;
    private String licencePlate;
    private String model;
    private String categoryName;
    private int year;
    private double currentOdometer;
    private VehicleStatus status;
    private BigDecimal dailyRentalRate;
    private long maintenanceCount;
    private BigDecimal totalMaintenanceCost;

    // Notice indicating rental metrics will integrate when Booking module is implemented
    private String bookingIntegrationNote;
}

