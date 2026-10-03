package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Immutable post-rental condition record. Pickup damage remains on HandoverRecord. */
@Entity
@Table(name = "vehicle_return_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReturnInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "staff_id", nullable = false, length = 50)
    private String staffId;

    @Column(name = "ending_mileage", nullable = false)
    private Double endingMileage;

    @Column(name = "has_damage", nullable = false)
    private boolean hasDamage;

    @Column(name = "damage_description", length = 1000)
    private String damageDescription;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "inspected_at", nullable = false)
    private LocalDateTime inspectedAt;
}
