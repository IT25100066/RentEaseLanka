package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.SimulatorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulatorConfigRepository extends JpaRepository<SimulatorConfig, Long> {
    Optional<SimulatorConfig> findByTriggerPattern(String triggerPattern);
}
