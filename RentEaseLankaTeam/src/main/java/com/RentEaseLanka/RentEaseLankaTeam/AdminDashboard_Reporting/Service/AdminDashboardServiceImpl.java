package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.Service;2

import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.DashboardSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.MaintenanceReportDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.MaintenanceReportDTO.MaintenanceItemDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.NotificationDTO;
import com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO.VehiclePerformanceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Repository.MaintenanceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final VehicleRepository vehicleRepository;
        private final MaintenanceRepository maintenanceRecordRepository;

    public AdminDashboardServiceImpl(VehicleRepository vehicleRepository,
                                     MaintenanceRepository maintenanceRecordRepository) {
        this.vehicleRepository = vehicleRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary() {
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long rentedVehicles = vehicleRepository.countByStatus(VehicleStatus.RENTED);
        long underMaintenanceVehicles = vehicleRepository.countByStatus(VehicleStatus.UNDER_MAINTENANCE);
        long outOfServiceVehicles = vehicleRepository.countByStatus(VehicleStatus.OUT_OF_SERVICE);

        // Calculate utilization % safely (guarding against division by zero)
        double fleetUtilizationRate = (totalVehicles == 0)
                ? 0.0
                : Math.round(((double) rentedVehicles / totalVehicles * 100.0) * 100.0) / 100.0;

        // Maintenance statistics
        long totalMaintenanceRecords = maintenanceRecordRepository.count();
        long scheduledMaintenance = maintenanceRecordRepository.countByStatus(MaintenanceStatus.SCHEDULED);
        long inProgressMaintenance = maintenanceRecordRepository.countByStatus(MaintenanceStatus.IN_PROGRESS);
        long completedMaintenance = maintenanceRecordRepository.countByStatus(MaintenanceStatus.COMPLETED);
        long cancelledMaintenance = maintenanceRecordRepository.countByStatus(MaintenanceStatus.CANCELLED);

        // Sum total maintenance cost
        List<MaintenanceRecord> allRecords = maintenanceRecordRepository.findAll();
        BigDecimal totalMaintenanceCost = allRecords.stream()
                .map(MaintenanceRecord::getCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryDTO.builder()
                .totalVehicles(totalVehicles)
                .availableVehicles(availableVehicles)
                .rentedVehicles(rentedVehicles)
                .underMaintenanceVehicles(underMaintenanceVehicles)
                .outOfServiceVehicles(outOfServiceVehicles)
                .fleetUtilizationRate(fleetUtilizationRate)
                .totalMaintenanceRecords(totalMaintenanceRecords)
                .scheduledMaintenance(scheduledMaintenance)
                .inProgressMaintenance(inProgressMaintenance)
                .completedMaintenance(completedMaintenance)
                .cancelledMaintenance(cancelledMaintenance)
                .totalMaintenanceCost(totalMaintenanceCost)
                .bookingIntegrationNotice("Active bookings and reservation metrics are pending integration with the Booking module.")
                .revenueIntegrationNotice("Revenue and payment calculations are pending integration with the Payment module.")
                .build();
    }

    @Override
    public List<VehiclePerformanceDTO> getAllVehiclePerformance() {
        List<Vehicle> vehicles = vehicleRepository.findAllWithCategory();
        if (vehicles.isEmpty()) {
            return Collections.emptyList();
        }

        // Group maintenance records by vehicle ID to avoid N+1 queries
        List<MaintenanceRecord> allMaintenance = maintenanceRecordRepository.findAll();
        Map<Long, List<MaintenanceRecord>> recordsByVehicle = allMaintenance.stream()
                .filter(m -> m.getVehicle() != null && m.getVehicle().getId() != null)
                .collect(Collectors.groupingBy(m -> m.getVehicle().getId()));

        return vehicles.stream().map(vehicle -> {
            List<MaintenanceRecord> records = recordsByVehicle.getOrDefault(vehicle.getId(), Collections.emptyList());
            long maintenanceCount = records.size();
            BigDecimal totalMaintenanceCost = records.stream()
                    .map(MaintenanceRecord::getCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String categoryName = (vehicle.getCategory() != null) ? vehicle.getCategory().getName() : "Uncategorized";

            return VehiclePerformanceDTO.builder()
                    .vehicleId(vehicle.getId())
                    .licencePlate(vehicle.getLicensePlate())
                    .model(vehicle.getModel())
                    .categoryName(categoryName)
                    .year(vehicle.getYear())
                    .currentOdometer(vehicle.getCurrentOdometer())
                    .status(vehicle.getStatus())
                    .dailyRentalRate(vehicle.getDailyRentalRate())
                    .maintenanceCount(maintenanceCount)
                    .totalMaintenanceCost(totalMaintenanceCost)
                    .bookingIntegrationNote("Rental frequency and revenue will integrate automatically when the Booking module is connected.")
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public VehiclePerformanceDTO getVehiclePerformanceById(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with ID: " + vehicleId));

        List<MaintenanceRecord> records = maintenanceRecordRepository.findAll().stream()
                .filter(m -> m.getVehicle() != null && vehicleId.equals(m.getVehicle().getId()))
                .toList();

        long maintenanceCount = records.size();
        BigDecimal totalMaintenanceCost = records.stream()
                .map(MaintenanceRecord::getCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String categoryName = (vehicle.getCategory() != null) ? vehicle.getCategory().getName() : "Uncategorized";

        return VehiclePerformanceDTO.builder()
                .vehicleId(vehicle.getId())
                .licencePlate(vehicle.getLicensePlate())
                .model(vehicle.getModel())
                .categoryName(categoryName)
                .year(vehicle.getYear())
                .currentOdometer(vehicle.getCurrentOdometer())
                .status(vehicle.getStatus())
                .dailyRentalRate(vehicle.getDailyRentalRate())
                .maintenanceCount(maintenanceCount)
                .totalMaintenanceCost(totalMaintenanceCost)
                .bookingIntegrationNote("Rental frequency and revenue will integrate automatically when the Booking module is connected.")
                .build();
    }

    @Override
    public List<NotificationDTO> getCriticalNotifications() {
        List<NotificationDTO> notifications = new ArrayList<>();
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long underMaintenance = vehicleRepository.countByStatus(VehicleStatus.UNDER_MAINTENANCE);
        long outOfService = vehicleRepository.countByStatus(VehicleStatus.OUT_OF_SERVICE);

        // 1. Low fleet availability alert
        if (totalVehicles == 0) {
            notifications.add(NotificationDTO.builder()
                    .id("NOTIF-FLEET-EMPTY")
                    .type("LOW_FLEET_AVAILABILITY")
                    .severity("INFO")
                    .title("Empty Fleet")
                    .message("No vehicles registered in inventory yet.")
                    .timestamp(LocalDateTime.now())
                    .build());
        } else {
            double percentAvailable = ((double) availableVehicles / totalVehicles) * 100.0;
            if (percentAvailable < 20.0 || availableVehicles <= 1) {
                notifications.add(NotificationDTO.builder()
                        .id("NOTIF-LOW-AVAILABILITY")
                        .type("LOW_FLEET_AVAILABILITY")
                        .severity("WARNING")
                        .title("Low Fleet Availability")
                        .message(String.format("Only %d of %d vehicles (%.1f%%) are available for rental.",
                                availableVehicles, totalVehicles, percentAvailable))
                        .timestamp(LocalDateTime.now())
                        .build());
            }
        }

        // 2. Vehicles currently under maintenance alert
        if (underMaintenance > 0) {
            notifications.add(NotificationDTO.builder()
                    .id("NOTIF-UNDER-MAINTENANCE")
                    .type("VEHICLE_UNDER_MAINTENANCE")
                    .severity("INFO")
                    .title("Vehicles Under Maintenance")
                    .message(underMaintenance + " vehicle(s) are currently under maintenance.")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 3. Out-of-service vehicles alert
        if (outOfService > 0) {
            notifications.add(NotificationDTO.builder()
                    .id("NOTIF-OUT-OF-SERVICE")
                    .type("OUT_OF_SERVICE")
                    .severity("CRITICAL")
                    .title("Out of Service Alert")
                    .message(outOfService + " vehicle(s) are marked OUT_OF_SERVICE and require inspection.")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        // 4. Module integration status notice
        notifications.add(NotificationDTO.builder()
                .id("NOTIF-INTEGRATION-STATUS")
                .type("MODULE_INTEGRATION_NOTICE")
                .severity("INFO")
                .title("Module Integration Notice")
                .message("Overdue returns and pending payment alerts will automatically activate once Booking & Payment modules are connected.")
                .timestamp(LocalDateTime.now())
                .build());

        return notifications;
    }

    @Override
    public MaintenanceReportDTO getMaintenanceReport(LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate cannot be after endDate");
        }

        List<MaintenanceRecord> records;
        if (startDate != null || endDate != null) {
            LocalDateTime startDateTime = (startDate != null)
                    ? startDate.atStartOfDay()
                    : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime endDateTime = (endDate != null)
                    ? endDate.atTime(LocalTime.MAX)
                    : LocalDateTime.of(2099, 12, 31, 23, 59, 59);

            records = maintenanceRecordRepository.findByScheduledDateBetween(startDateTime, endDateTime);
        } else {
            records = maintenanceRecordRepository.findAllWithVehicle();
        }

        long totalRecords = records.size();
        long scheduledCount = records.stream().filter(r -> r.getStatus() == MaintenanceStatus.SCHEDULED).count();
        long inProgressCount = records.stream().filter(r -> r.getStatus() == MaintenanceStatus.IN_PROGRESS).count();
        long completedCount = records.stream().filter(r -> r.getStatus() == MaintenanceStatus.COMPLETED).count();
        long cancelledCount = records.stream().filter(r -> r.getStatus() == MaintenanceStatus.CANCELLED).count();

        BigDecimal totalCost = records.stream()
                .map(MaintenanceRecord::getCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MaintenanceItemDTO> itemDTOs = records.stream().map(r -> MaintenanceItemDTO.builder()
                .recordId(r.getId())
                .vehicleId((r.getVehicle() != null) ? r.getVehicle().getId() : null)
                .licencePlate((r.getVehicle() != null) ? r.getVehicle().getLicensePlate() : "N/A")
                .vehicleModel((r.getVehicle() != null) ? r.getVehicle().getModel() : "N/A")
                .maintenanceType((r.getType() != null) ? r.getType().name() : "N/A")
                .maintenanceStatus((r.getStatus() != null) ? r.getStatus().name() : "N/A")
                .description(r.getDescription())
                .cost(r.getCost())
                .odometerAtService(r.getOdometerAtService())
                .scheduledDate(r.getScheduledDate())
                .completionDate(r.getCompletionDate())
                .build()
        ).collect(Collectors.toList());

        return MaintenanceReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRecords(totalRecords)
                .scheduledCount(scheduledCount)
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .totalCost(totalCost)
                .records(itemDTOs)
                .build();
    }
}
