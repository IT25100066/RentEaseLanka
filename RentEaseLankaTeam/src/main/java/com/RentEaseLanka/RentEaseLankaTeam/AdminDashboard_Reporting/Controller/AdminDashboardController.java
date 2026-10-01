package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.DashboardSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.MaintenanceReportDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.NotificationDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.VehiclePerformanceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.Service.AdminDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * Endpoint 1: Summary overview of fleet inventory, utilization rate, and maintenance statistics.
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        return ResponseEntity.ok(adminDashboardService.getDashboardSummary());
    }

    /**
     * Endpoint 2: List of all vehicles with odometer readings, status, and maintenance counts.
     */
    @GetMapping("/dashboard/vehicles")
    public ResponseEntity<List<VehiclePerformanceDTO>> getAllVehicles() {
        return ResponseEntity.ok(adminDashboardService.getAllVehiclePerformance());
    }

    /**
     * Endpoint 3: Performance and maintenance statistics for a specific vehicle by ID.
     */
    @GetMapping("/dashboard/vehicles/{id}")
    public ResponseEntity<VehiclePerformanceDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(adminDashboardService.getVehiclePerformanceById(id));
    }

    /**
     * Endpoint 4: Active critical notifications and system status alerts.
     */
    @GetMapping("/dashboard/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        return ResponseEntity.ok(adminDashboardService.getCriticalNotifications());
    }

    /**
     * Endpoint 5: Maintenance report with optional date filtering (?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD).
     */
    @GetMapping("/reports/maintenance")
    public ResponseEntity<MaintenanceReportDTO> getMaintenanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminDashboardService.getMaintenanceReport(startDate, endDate));
    }
}
