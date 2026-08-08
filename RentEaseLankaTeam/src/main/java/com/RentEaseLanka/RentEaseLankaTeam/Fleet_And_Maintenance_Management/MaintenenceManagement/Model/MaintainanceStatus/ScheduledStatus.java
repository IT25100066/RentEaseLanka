package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.AvailableState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.MaintenanceState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduledStatus implements VehicleMaintenanceState {


    @Override
    public void scheduledState(MaintenanceRecord record) {
        throw new IllegalStateException("Vehicle is Alredy in Scheduled state");

    }

    @Override
    public void inProgressState( MaintenanceRecord record) {
        record.setStatus(MaintenanceStatus.IN_PROGRESS);
        record.getVehicle().changeState(VehicleStatus.UNDER_MAINTENANCE,new MaintenanceState());
        record.setCurrentState(new InProgressStatus());
    }

    @Override
    public void completeState( MaintenanceRecord record) {
        throw new IllegalStateException("Cannot complete maintenance directly from SCHEDULED state. Must be IN_PROGRESS first.");
    }

    @Override
    public void cancelState( MaintenanceRecord record) {
        record.setStatus(MaintenanceStatus.CANCELLED);
        record.getVehicle().changeState(VehicleStatus.AVAILABLE, new AvailableState());
        record.setCurrentState(new CanceledStatus());
    }
}
