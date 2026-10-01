package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    // Fleet inventory metrics
    private long totalVehicles;
    private long availableVehicles;
    private long rentedVehicles;
    private long underMaintenanceVehicles;
    private long outOfServiceVehicles;

    // Fleet utilization % = (rented / total) * 100
    private double fleetUtilizationRate;

    // Maintenance metrics
    private long totalMaintenanceRecords;
    private long scheduledMaintenance;
    private long inProgressMaintenance;
    private long completedMaintenance;
    private long cancelledMaintenance;
    private BigDecimal totalMaintenanceCost;

    // Future integration notices (to be linked with Booking and Payment modules)
    private String bookingIntegrationNotice;
    private String revenueIntegrationNotice;
}
