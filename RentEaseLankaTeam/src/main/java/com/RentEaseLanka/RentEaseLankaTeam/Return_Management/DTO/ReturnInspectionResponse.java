package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO;

import java.time.LocalDateTime;

public record ReturnInspectionResponse(Long id, Long bookingId, Long vehicleId, String staffId,
                                       Double endingMileage, boolean hasDamage, String damageDescription,
                                       String notes, LocalDateTime inspectedAt) { }
