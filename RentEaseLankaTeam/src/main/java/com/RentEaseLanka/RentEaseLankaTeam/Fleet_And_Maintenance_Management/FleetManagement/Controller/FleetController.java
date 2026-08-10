package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateVehicleRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FleetController {

    @Autowired
    private FleetService fleetService;

    @RequestMapping(value ="/vehicle/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addVehicle( @Valid @ModelAttribute VehicleCreateRequestDTO newVehicle){

        try{

            fleetService.createVehicleWithImages(newVehicle);
            return new ResponseEntity<>("Vehicle created Successfully",HttpStatus.CREATED);


        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // GET all vehicles with their image URLs
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        try {
            List<VehicleResponseDTO> vehicles = fleetService.getAllVehicles();
            return new ResponseEntity<>(vehicles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET a single vehicle by ID with its image URLs
    @GetMapping("/vehicle/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        try {
            VehicleResponseDTO vehicle = fleetService.getVehicleById(id);
            return new ResponseEntity<>(vehicle, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/vehicle/update")
    public ResponseEntity<?> updateVehicle(@RequestBody UpdateVehicleRequestDTO dto){

        boolean updated = fleetService.updateVehicle(dto);
        if (updated){
            return new ResponseEntity<>("Vehicle Updated Successfully",HttpStatus.ACCEPTED);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/pendingVehicles")
    public ResponseEntity<List<VehicleResponseDTO>> pendingVehicles(){
        try{
            List<VehicleResponseDTO> vehicles =  fleetService.pendingVehicles();
            if(vehicles.isEmpty()){
                log.info("No pending vehicles present");
            }
            return new ResponseEntity<>(vehicles,HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/vehicle/makeAvailable")
    public ResponseEntity<String> makeVehicleAvailable(@RequestBody List<Long> ids){
        try{
            log.info(ids.toString());

            boolean done = fleetService.makeAvailable(ids);
            if (done){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/vehicle")
    public ResponseEntity<?> GetVehicleByFilter(@RequestParam("filterBy") String filter ,
                                                                       @Nullable @RequestParam("category") String category ,
                                                                         @Nullable @RequestParam("status") String status){

        String filterType = filter.toLowerCase();
        try{
            List<VehicleResponseDTO>  vehicles = fleetService.getVehiclesByFilter(filterType,category, status);
            return new ResponseEntity<>(vehicles,HttpStatus.OK);

        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/vehicle/{id}/remove")
    public ResponseEntity<?> setVehicleToOutOfService(@PathVariable Long id){
        try{
            boolean outOfService = fleetService.setVehicleToOutOfService(id);
            if(outOfService){
                return new ResponseEntity<>("Vehicle is set to Out Of Service",HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return null;
    }


}
