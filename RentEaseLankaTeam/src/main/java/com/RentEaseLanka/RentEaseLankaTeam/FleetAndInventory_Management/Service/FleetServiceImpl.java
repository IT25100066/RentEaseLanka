package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCategoryCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.VehicleCreateRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleImage;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleCategoryRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleImageRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service.FileStorageService.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FleetServiceImpl implements FleetService {


    @Autowired
    private  VehicleRepository vehicleRepository;
    @Autowired
    private  VehicleImageRepository vehicleImageRepository;
    @Autowired
    private  FileStorageService fileStorageService;
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;



    @Transactional
    public void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest) {

        String licensePlate = vehicleCreateRequest.getLicensePlate();
        String model = vehicleCreateRequest.getModel();
        List<MultipartFile> Images = vehicleCreateRequest.getImages();
        int year = vehicleCreateRequest.getYear();
        BigDecimal dailyRent = vehicleCreateRequest.getDailyRate();
        VehicleCategory category = vehicleCategoryRepository.getVehicleCategoriesById(vehicleCreateRequest.getCategoryId());

        // 1. Business Validation: Prevent duplicate license plates
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("Validation Error: A vehicle with license plate " + licensePlate + " already exists.");
        }

        // 2. Persist the Parent Entity (Vehicle)
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(model);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setYear(year);
        vehicle.setDailyRentalRate(dailyRent);
        vehicle.setCurrentOdometer(0.0);
        vehicle.setCategory(category);
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
    }

    @Override
    public VehicleCategory createCategory(VehicleCategoryCreateRequestDTO dto) {

        boolean isCategoryPresent = vehicleCategoryRepository.findCategoryByName(dto.getName()).isPresent();

        try{
            if (!isCategoryPresent){
                VehicleCategory category = new VehicleCategory();
                category.setBasePrice(dto.getBasePrice());
                category.setName(dto.getName());
                category.setDescription(dto.getDescription());
                vehicleCategoryRepository.save(category);
                return category;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        return null;

    }


}
