package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.MaintenanceState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;

public class CanceledStatus implements VehicleMaintenanceState{

    @Override
    public void scheduledState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot Scheduled a CANCELLED maintenance record.");
    }

    @Override
    public void inProgressState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot Process a CANCELLED maintenance record.");
    }

    @Override
    public void completeState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot complete a CANCELLED maintenance record.");
    }

    @Override
    public void cancelState(MaintenanceRecord record) {
        throw new IllegalStateException("Maintenance record is already CANCELLED.");
    }
}
