package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaintenanceRecordDTO {

    private Long vehicleId;
    private String type;
    private String description;
    private Double odometerAtService;
    private LocalDateTime scheduledDate;



}
