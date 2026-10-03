package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleReservationId;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal assessedDamageAmount;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String damageDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCharged = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime assessedAt = LocalDateTime.now();
}

