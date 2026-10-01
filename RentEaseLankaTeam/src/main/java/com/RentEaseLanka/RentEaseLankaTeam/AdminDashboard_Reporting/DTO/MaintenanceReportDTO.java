package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceReportDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalRecords;
    private long scheduledCount;
    private long inProgressCount;
    private long completedCount;
    private long cancelledCount;
    private BigDecimal totalCost;

    @Builder.Default
    private List<MaintenanceItemDTO> records = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceItemDTO {
        private Long recordId;
        private Long vehicleId;
        private String licencePlate;
        private String vehicleModel;
        private String maintenanceType;
        private String maintenanceStatus;
        private String description;
        private BigDecimal cost;
        private Double odometerAtService;
        private LocalDateTime scheduledDate;
        private LocalDateTime completionDate;
    }
}
