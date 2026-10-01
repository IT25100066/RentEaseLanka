package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.Service;

import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.DashboardSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.MaintenanceReportDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.NotificationDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.VehiclePerformanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminDashboardService {

    DashboardSummaryDTO getDashboardSummary();

    List<VehiclePerformanceDTO> getAllVehiclePerformance();

    VehiclePerformanceDTO getVehiclePerformanceById(Long vehicleId);

    List<NotificationDTO> getCriticalNotifications();

    MaintenanceReportDTO getMaintenanceReport(LocalDate startDate, LocalDate endDate);
}
