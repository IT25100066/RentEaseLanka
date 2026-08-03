package com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.DTOs.*;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleImage;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleCategoryRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleImageRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.FleetAndInventory_Management.Service.FileStorageService.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
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
        vehicle.setStatus(VehicleStatus.PENDING);
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
    public List<VehicleResponseDTO> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        return mapToResponseDTO(vehicle);
    }

    @Override
    public boolean updateVehicle(UpdateVehicleRequestDTO dto) {

        try{
                Optional<Vehicle> vehicle = vehicleRepository.findById(dto.getId());
                if (vehicle.isEmpty()){
                    log.info("Vehicle not present in the Database");
                    return false;
                }
                Vehicle vehicle1 = vehicleRepository.getReferenceById(dto.getId());
                vehicle1.setCategory(vehicleCategoryRepository.getVehicleCategoriesById(dto.getCategoryId()));
                vehicle1.setDailyRentalRate(dto.getDailyRentalRate());
                vehicle1.setModel(dto.getModel());
                vehicle1.setUpdatedAt(LocalDateTime.now());

                log.info("Vehicle Updated : Vehicle id "+ dto.getId());
                vehicleRepository.save(vehicle1);
                return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean updateOdometer(UpdateOdometerDTO dto){
        try{
            Vehicle vehicle = vehicleRepository.findById(dto.getId()).orElseThrow();
            vehicle.setCurrentOdometer(dto.getCurrentOdometer());
            log.info("Odometer Updated Successfully");
            return true;

        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;


    }


    /**
     * Maps a Vehicle entity to a VehicleResponseDTO.
     * Flattens category info and collects image file paths as URLs.
     */
    private VehicleResponseDTO mapToResponseDTO(Vehicle vehicle) {

        // Collect image URLs from the vehicle's images
        List<String> imageUrls = vehicle.getImages() != null
                ? vehicle.getImages().stream()
                    .map(VehicleImage::getFilePath)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return VehicleResponseDTO.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .currentOdometer(vehicle.getCurrentOdometer())
                .status(vehicle.getStatus() != null ? vehicle.getStatus().name() : null)
                .dailyRentalRate(vehicle.getDailyRentalRate())
                .categoryId(vehicle.getCategory() != null ? vehicle.getCategory().getId() : null)
                .categoryName(vehicle.getCategory() != null ? vehicle.getCategory().getName() : null)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .imageUrls(imageUrls)
                .build();
    }


}

