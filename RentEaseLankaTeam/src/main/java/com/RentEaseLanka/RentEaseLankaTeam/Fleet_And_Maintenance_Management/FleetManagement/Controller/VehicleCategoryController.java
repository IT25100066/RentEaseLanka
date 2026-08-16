package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.CategoryResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.UpdateCategoryPriceDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service.VehicleCategoryService;
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
            throw new RuntimeException("Cannot Create Vehicle Category",e);
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

    @PostMapping("/update")
    public ResponseEntity<String> updateCategoryPricing(@RequestBody UpdateCategoryPriceDTO dto){
        boolean updated = vehicleCategoryService.UpdateCategoryPrice(dto);
        if (updated){
            return new ResponseEntity<>("Category Updated",HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }





}
