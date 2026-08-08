package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;

public interface VehicleMaintenanceState {
    
    void scheduledState( MaintenanceRecord record);
    void inProgressState( MaintenanceRecord record);
    void completeState ( MaintenanceRecord record);
    void cancelState ( MaintenanceRecord record);
}
