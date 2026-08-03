package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service.VehicleCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class VehicleCategoryController {

    @Autowired
   private  VehicleCategoryService vehicleCategoryService ;

    //create a vehicle category
    @PostMapping("/create")
    public ResponseEntity<VehicleCategory> createCategory(@Valid @RequestBody VehicleCategoryCreateRequestDTO dto) {

        try{

            VehicleCategory category = vehicleCategoryService.createCategory(dto);
            return new ResponseEntity<>(category, HttpStatus.CREATED);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping
    public ResponseEntity<?> allCategories(){

        try{

            List<CategoryResponseDTO> responses = vehicleCategoryService.getAllCategories();
            return new ResponseEntity<>(responses,HttpStatus.OK);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }





}
