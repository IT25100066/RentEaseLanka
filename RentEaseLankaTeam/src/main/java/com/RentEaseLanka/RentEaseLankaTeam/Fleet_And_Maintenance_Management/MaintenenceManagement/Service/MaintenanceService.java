package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Service;


import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceCompleteDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceRecordDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;

import java.util.List;

public interface MaintenanceService {
    boolean scheduleMaintenance(MaintenanceRecordDTO record);

    List<MaintenanceRecord> getRecordsForVehicle(Long id);

    MaintenanceRecord getMaintenanceRecordById(Long id);

    boolean updateStatus(Long id);

    boolean updateStatus(Long id, MaintenanceCompleteDTO dto);

    boolean cancelMaintenance(Long id);
}
