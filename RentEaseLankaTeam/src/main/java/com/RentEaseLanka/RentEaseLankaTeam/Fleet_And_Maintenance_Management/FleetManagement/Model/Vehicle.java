package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.*;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(unique = true , nullable = false, length = 20)
    private String licensePlate;

    @NonNull
    @Column( nullable = false, length = 50)
    private String model;

    @Min(1990)
    @Column(name = "manufacture_year", nullable = false)
    private int year ;

    @PositiveOrZero
    @Column(nullable = false)
    private double currentOdometer;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_status", nullable = false, length = 30)
    private VehicleStatus status;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRentalRate;

    // Relational Mapping: Many vehicles belong to one category
    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategory category;

    // Relational Mapping: One vehicle has many maintenance records
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Transient
    private VehicleState currentState;

    //Link Vehicle Image Entity which consist a list of VehicleImage objects
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleImage> images = new ArrayList<>();

    private VehicleState resolveState(VehicleStatus status) {
        return switch (status) {
            case AVAILABLE      -> new AvailableState();
            case RESERVED       -> new ReservedState();
            case PENDING        -> new PendingState();
            case RENTED         -> new RentedState();
            case UNDER_MAINTENANCE    -> new MaintenanceState();
            case OUT_OF_SERVICE -> new OutOfServiceState();
        };
    }

    private VehicleState getState() {
        if (currentState == null) {
            currentState = resolveState(this.status);
        }
        return currentState;
    }

    public void changeState(VehicleStatus newStatus, VehicleState newState) {
        this.status = newStatus;
        this.currentState = newState;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            changeState(VehicleStatus.PENDING, new PendingState());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    //Change the vehicle state

    public void reserve()             { getState().reserve(this); }
    public void rent()                { getState().rent(this); }
    public void returnVehicle()       { getState().returnVehicle(this); }
    public void scheduleMaintenance() { getState().scheduleMaintenance(this); }
    public void makeAvailable()       { getState().makeAvailable(this); }
    public void outOfService()        { getState().outOfService(this); }
    public void pending()             { getState().pendingState(this);}

}
