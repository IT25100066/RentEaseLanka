package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleImage;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleImageRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service.FileStorageService.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class FleetServiceImpl implements FleetService {


    @Autowired
    private final VehicleRepository vehicleRepository;
    @Autowired
    private final VehicleImageRepository vehicleImageRepository;
    @Autowired
    private FileStorageService fileStorageService;



    @Transactional
    public Vehicle createVehicleWithImages( VehicleRequestDTO vehicleCreateRequest) {

        String licensePlate = vehicleCreateRequest.getLicensePlate();
        String model = vehicleCreateRequest.getModel();
        List<MultipartFile> Images = vehicleCreateRequest.getImages();
        int year = vehicleCreateRequest.getYear();

        // 1. Business Validation: Prevent duplicate license plates
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("Validation Error: A vehicle with license plate " + licensePlate + " already exists.");
        }

        // 2. Persist the Parent Entity (Vehicle)
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(model);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setYear(year);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // 3. Delegate file system operations to the FileStorageService
        List<String> relativeImagePaths = fileStorageService.storeFiles(savedVehicle.getId(), Images);

        // 4. Persist the Child Entities (VehicleImages)
        List<VehicleImage> imageEntities = new ArrayList<>();

        for (String path : relativeImagePaths) {
            VehicleImage img = new VehicleImage();
            img.setVehicle(savedVehicle);
            img.setFilePath(path);

            // Extract just the file name from the relative path
            img.setFileName(path.substring(path.lastIndexOf("/") + 1));
            imageEntities.add(img);
        }

        vehicleImageRepository.saveAll(imageEntities);

        // Return the fully constructed entity to the Controller
        return savedVehicle;
    }



}
