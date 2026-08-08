package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relational Mapping: Many maintenance records belong to one vehicle
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status;

    @Column(length = 500)
    private String description;

    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2 ,nullable = true)
    private BigDecimal cost;

    @PositiveOrZero
    private Double odometerAtService;

    private LocalDateTime scheduledDate;
    @Column(nullable = true)
    private LocalDateTime completionDate;

    @PostLoad
    public void initializeState() {
        if (this.status == null) return;
        switch (this.status) {
            case SCHEDULED -> this.currentState = new ScheduledStatus();
            case IN_PROGRESS -> this.currentState = new InProgressStatus();
            case COMPLETED -> this.currentState = new CompletedStatus();
            case CANCELLED -> this.currentState = new CanceledStatus();
        }
    }

    @Transient
    private VehicleMaintenanceState currentState;

    public void maintenanceStatusChange(VehicleMaintenanceState maintenanceState, MaintenanceStatus newStatus){
        this.currentState=maintenanceState;
        this.status=newStatus;

    }

    public void scheduleMaintenance(){this.currentState.scheduledState(this);}
    public void inProgressMaintenance(){this.currentState.inProgressState(this);}
    public void completeMaintenance(){this.currentState.completeState(this);}
    public void cancelMaintenance(){this.currentState.cancelState(this);}
}
