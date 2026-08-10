package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.AvailableState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.MaintenanceState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceCompleteDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceRecordDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintainanceStatus.ScheduledStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceType;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Repository.MaintenanceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MaintenanceServiceImpl implements MaintenanceService{

    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    MaintenanceRepository maintenanceRepository;


    //scheduling a vehicle maintenance
    @Override
    @Transactional
    public boolean scheduleMaintenance(MaintenanceRecordDTO record) {

        try{
            // checking the user request
            if (record == null){
                 throw new RuntimeException("Maintenance Record is null");
            }

            Vehicle vehicle = vehicleRepository.findById(record.getVehicleId()).orElseThrow();
            if (vehicle.getStatus().name().equalsIgnoreCase("PENDING")){
                log.info("Vehicle status is PENDING");
                throw new RuntimeException("Vehicle status is PENDING");

            }
            //checking weather the record is already recorded
            try{
               List<MaintenanceRecord> inProgressRecords=  maintenanceRepository.findByVehicleAndStatus(vehicle, MaintenanceStatus.SCHEDULED);
               List<MaintenanceRecord> scheduledRecords=  maintenanceRepository.findByVehicleAndStatus(vehicle, MaintenanceStatus.IN_PROGRESS);

                if (!inProgressRecords.isEmpty() || !scheduledRecords.isEmpty()){
                    log.info("A Maintenance is already scheduled {} , {}",inProgressRecords,scheduledRecords);
                    throw new RuntimeException("Maintenance Record already exists");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }


            //creating a new maintenance record
            MaintenanceRecord maintenanceRecord = new MaintenanceRecord();

            maintenanceRecord.setVehicle(vehicle);
            maintenanceRecord.setType(MaintenanceType.valueOf(record.getType().toUpperCase()));
            maintenanceRecord.setDescription(record.getDescription());
            maintenanceRecord.setOdometerAtService(record.getOdometerAtService());
            maintenanceRecord.setScheduledDate(LocalDateTime.now());
            maintenanceRecord.maintenanceStatusChange(new ScheduledStatus(), MaintenanceStatus.SCHEDULED);

            //changing the relevant vehicle's vehicle status
            vehicle.scheduleMaintenance();
            vehicleRepository.save(vehicle);
            maintenanceRepository.save(maintenanceRecord);

            log.info("Maintenance Record saved successfully");
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<MaintenanceRecord> getRecordsForVehicle(Long id) {
        try{
            Vehicle vehicle = vehicleRepository.findById(id).orElseThrow();
            List<MaintenanceRecord> records = maintenanceRepository.findByVehicle(vehicle);
            if (records == null){
                log.info("No Records found");
            }
            return records;
        }catch (Exception e){
            log.info(e.getMessage());
            return null;
        }

    }

    // return a Maintenance record on its id
    @Override
    public MaintenanceRecord getMaintenanceRecordById(Long id) {

        try{
            MaintenanceRecord record = maintenanceRepository.findById(id).orElseThrow();
            log.info("Maintenance Record get successfully "+ record.getVehicle().getLicensePlate());
            return record;
        } catch (Exception e) {
           log.info(e.getMessage());
           return null;
        }
    }

    //Change the maintenance status by MaintenanceId
    @Override
    @Transactional
    public boolean startMaintenance(Long id) {

        MaintenanceRecord record = maintenanceRepository.findById(id).orElseThrow();
        Vehicle vehicle = record.getVehicle();

        if (record.getStatus().equals(MaintenanceStatus.COMPLETED) || record.getStatus().equals(MaintenanceStatus.CANCELLED)){
            log.info("Maintenance Record Cannot be Updated (COMPLETED/CANCELLED) ");
            return false;
        }

        if(record.getStatus() == MaintenanceStatus.SCHEDULED){
            record.inProgressMaintenance();
            log.info("Maintenance Record  Updated to "+MaintenanceStatus.IN_PROGRESS.name());
            maintenanceRepository.save(record);
            return true;
        } else{
            return false;
        }
    }

    @Override
    @Transactional
    public boolean completeMaintenance(Long id, MaintenanceCompleteDTO dto) {

        MaintenanceRecord record = maintenanceRepository.findById(id).orElseThrow();

        try{
            Vehicle vehicle = vehicleRepository.findById(record.getVehicle().getId()).orElseThrow();
            record.completeMaintenance();
            record.setCost(dto.getCost());
            record.setCompletionDate(LocalDateTime.now());
            vehicle.makeAvailable();

            vehicleRepository.save(vehicle);
            log.info("Vehicle status updated successfully for Vehicle : {}", record.getVehicle().getLicensePlate());
            maintenanceRepository.save(record);
            log.info("Maintenance Record Updated to COMPLETE");
            return true;

        } catch (RuntimeException e) {
            throw new RuntimeException("Vehicle is already Completed maintenance");
        }

    }

    @Override
    @Transactional
    public boolean cancelMaintenance(Long id) {
        try{
            MaintenanceRecord record = maintenanceRepository.findById(id).orElseThrow();
            log.info(record.getType().toString());
            Vehicle vehicle = record.getVehicle();

                record.cancelMaintenance();
                vehicle.makeAvailable();
                vehicleRepository.save(vehicle);
                log.info("Maintenance Cancelled successfully for Vehicle : {}", vehicle.getLicensePlate());
                maintenanceRepository.save(record);
                return true;


        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }


}
