package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "handover_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandoverRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relational Mapping: Many handover records belong to one vehicle
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Decoupled foreign references for teammate modules (Booking, Customer)
    @Column(name = "booking_id", nullable = true)
    private Long bookingId;

    @Column(name = "customer_id", nullable = true)
    private Long customerId;

    @Column(name = "customer_name", length = 100, nullable = true)
    private String customerName;

    @Column(name = "staff_id", length = 50, nullable = false)
    private String staffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private HandoverStatus status;

    // Vehicle condition at handover
    @PositiveOrZero
    @Column(name = "starting_mileage")
    private Double startingMileage;

    @Column(name = "has_damage", nullable = false)
    private boolean hasDamage;

    // A newly initiated handover has not yet had its payment verified.
    // Use a primitive so Hibernate can never bind NULL to the NOT NULL column.
    @Column(name = "payment_verified", nullable = false)
    private boolean paymentVerified;

    @Column(name = "damage_description", length = 1000)
    private String damageDescription;

    @Column(name = "fuel_level", length = 30)
    private String fuelLevel;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "handover_date_time")
    private LocalDateTime handoverDateTime;

    @Column(name = "completion_date_time")
    private LocalDateTime completionDateTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.handoverDateTime == null) {
            this.handoverDateTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = HandoverStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
