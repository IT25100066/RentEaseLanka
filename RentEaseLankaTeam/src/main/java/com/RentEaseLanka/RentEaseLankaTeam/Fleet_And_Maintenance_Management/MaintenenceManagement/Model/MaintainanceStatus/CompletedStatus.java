package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;

public class CompletedStatus implements VehicleMaintenanceState{

    @Override
    public void scheduledState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot restart a Sheduled maintenance record.");
    }

    @Override
    public void inProgressState(MaintenanceRecord record) {
        throw new IllegalStateException("Vehicle is alredy Completed the maintenance");
    }

    @Override
    public void completeState(MaintenanceRecord record) {
        throw new IllegalStateException("Maintenance record is already COMPLETED.");
    }

    @Override
    public void cancelState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot cancel a maintenance record that is already COMPLETED.");
    }
}
