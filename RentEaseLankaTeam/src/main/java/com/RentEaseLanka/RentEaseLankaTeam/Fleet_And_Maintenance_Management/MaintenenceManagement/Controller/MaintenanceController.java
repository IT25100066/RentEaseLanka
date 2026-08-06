package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceCompleteDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.DTO.MaintenanceRecordDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Model.MaintenanceType;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Service.MaintenanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<String> scheduleMaintenance(@RequestBody MaintenanceRecordDTO record ) {
        try{
            boolean result = maintenanceService.scheduleMaintenance(record);
            if (result){
                log.info("Maintenance Scheduled : Vehicle {} to {}", record.getVehicleId(), MaintenanceType.valueOf(record.getType()));
                return new ResponseEntity<>("Maintenance Scheduled Successfully", HttpStatus.OK);
            }else{
                return new  ResponseEntity<>("Maintenance Scheduled Failed", HttpStatus.NOT_ACCEPTABLE);
            }
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<List<MaintenanceRecord>> getAllMaintenanceRecords(@PathVariable Long id ) {
        try{
            List<MaintenanceRecord> records = maintenanceService.getRecordsForVehicle(id);
            return new ResponseEntity<>(records, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?>  getMaintenanceRecord(@PathVariable Long id ) {
        try{
            MaintenanceRecord record =  maintenanceService.getMaintenanceRecordById(id);
            if(record == null){
                return new ResponseEntity<>("Record not present!", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(record, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Operation failed Please Try again! ",HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateMaintenanceStatus(@PathVariable Long id ) {
        try{
            boolean updated = maintenanceService.updateStatus(id);
            if (updated){
                log.info("Maintenance Status Updated : Vehicle {}  ", id);
                return new ResponseEntity<>("Maintenance Status Updated", HttpStatus.OK);
            }
            return new ResponseEntity<>("Maintenance Status Updated Failed", HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Operation failed Please Try again! ",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/completed")
    public ResponseEntity<String> updateMaintenanceStatus(@PathVariable Long id, @RequestBody MaintenanceCompleteDTO dto ) {
        boolean updated = maintenanceService.updateStatus(id,dto);
        if (updated){
            return new ResponseEntity<>("Maintenance Status Is Updated to Completed", HttpStatus.OK);
        }
        return new ResponseEntity<>("Maintenance Status Is Updated to Failed", HttpStatus.NOT_ACCEPTABLE);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelMaintenance(@PathVariable Long id) {
        try{
            boolean cancelled = maintenanceService.cancelMaintenance(id);
            if (cancelled){
                log.info("Maintenance Cancelled : Vehicle {}  ", id);
                return new ResponseEntity<>("Maintenance Cancelled", HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Server Error while canceling the Maintenance Record");
            return new ResponseEntity<>("Operation failed Please Try again! ",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
        }






}
