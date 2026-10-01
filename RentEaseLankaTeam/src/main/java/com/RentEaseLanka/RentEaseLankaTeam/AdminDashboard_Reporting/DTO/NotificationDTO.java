package com.RentEaseLanka.RentEaseLankaTeam.AdminDashboard_Reporting.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private String type;     // LOW_FLEET_AVAILABILITY, VEHICLE_UNDER_MAINTENANCE, OUT_OF_SERVICE, MODULE_INTEGRATION_NOTICE
    private String severity; // INFO, WARNING, CRITICAL
    private String title;
    private String message;
    private LocalDateTime timestamp;
}
