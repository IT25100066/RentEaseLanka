package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "simulator_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String triggerPattern;

    @Column(nullable = false, length = 50)
    private String responseCode;

    @Column(nullable = false)
    private Long latencyMs;
}
