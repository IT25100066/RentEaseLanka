package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.AvailableState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;

import java.time.LocalDateTime;

public class InProgressStatus implements VehicleMaintenanceState{

    @Override
    public void scheduledState(MaintenanceRecord record) {
        throw new IllegalStateException("Maintenance is already Scheduled and in Progress");
    }

    @Override
    public void inProgressState(MaintenanceRecord record) {
        throw new IllegalStateException("Maintenance is already IN_PROGRESS.");
    }

    @Override
    public void completeState(MaintenanceRecord record) {
        record.setStatus(MaintenanceStatus.COMPLETED);
        record.setCompletionDate(LocalDateTime.now());
        record.getVehicle().changeState(VehicleStatus.AVAILABLE, new AvailableState());
        record.setCurrentState(new CompletedStatus());
    }

    @Override
    public void cancelState(MaintenanceRecord record) {
        throw new IllegalStateException("Cannot Cancel the maintenance (Already in Progress)");

    }
}
