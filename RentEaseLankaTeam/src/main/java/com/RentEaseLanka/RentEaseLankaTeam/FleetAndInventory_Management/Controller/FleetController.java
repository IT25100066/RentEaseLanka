package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    //create a vehicle category
    @PostMapping("/category/create")
    public ResponseEntity<VehicleCategory> createCategory(@Valid @RequestBody VehicleCategoryCreateRequestDTO dto) {

        try{

            VehicleCategory category = fleetService.createCategory(dto);
            return new ResponseEntity<>(category,HttpStatus.CREATED);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
