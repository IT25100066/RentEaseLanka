package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO.*;

import java.util.List;

public interface HandoverService {

    // CRUD Operations
    HandoverResponseDTO initiateHandover(HandoverCreateRequestDTO dto);

    List<HandoverResponseDTO> getAllHandovers(String status);

    HandoverResponseDTO getHandoverById(Long id);

    List<HandoverResponseDTO> getHandoversByVehicleId(Long vehicleId);

    HandoverResponseDTO updateHandover(Long id, HandoverUpdateRequestDTO dto);

    boolean deleteHandover(Long id);



    HandoverResponseDTO recordMileage(Long id, MileageRecordDTO dto);

    HandoverResponseDTO recordDamage(Long id, DamageInspectionDTO dto);

    HandoverResponseDTO completeHandover(Long id);

    HandoverResponseDTO cancelHandover(Long id);
}
