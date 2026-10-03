package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.DTOs.*;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Mappers.VehicleMapper;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleCategory;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleImage;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleCategoryRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleImageRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service.FileStorageService.FileStorageService;

import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FleetServiceImpl implements FleetService {

    @Autowired
    private final VehicleRepository vehicleRepository;
    @Autowired
    private final VehicleImageRepository vehicleImageRepository;
    @Autowired
    private final FileStorageService fileStorageService;
    @Autowired
    private final VehicleCategoryRepository vehicleCategoryRepository;
    @Autowired(required = true)
    private final VehicleMapper vehicleMapper;

    @Transactional
    @CacheEvict(value = "Vehicles", key = "'allVehicles'")
    public void createVehicleWithImages(VehicleCreateRequestDTO vehicleCreateRequest) {

        String licensePlate = vehicleCreateRequest.getLicensePlate();
        String model = vehicleCreateRequest.getModel();
        List<MultipartFile> Images = vehicleCreateRequest.getImages();
        int year = vehicleCreateRequest.getYear();
        BigDecimal dailyRent = vehicleCreateRequest.getDailyRate();
        double currentOdometer = vehicleCreateRequest.getCurrentOdometer();
        VehicleCategory category = vehicleCategoryRepository
                .getVehicleCategoriesById(vehicleCreateRequest.getCategoryId());

        // 1. Business Validation: Prevent duplicate license plates
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException(
                    "Validation Error: A vehicle with license plate " + licensePlate + " already exists.");
        }

        // 2. Persist the Parent Entity (Vehicle)
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(model);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setYear(year);
        vehicle.setDailyRentalRate(dailyRent);
        vehicle.setCurrentOdometer(currentOdometer);
        vehicle.setCategory(category);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle Created Successfully {} ", vehicle.getLicensePlate());

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
    @Transactional
    @CacheEvict(value = "Vehicles", key = "'allVehicles'")
    public List<String> addVehicleImages(Long vehicleId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("No images provided for upload.");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        List<String> relativeImagePaths = fileStorageService.storeFiles(vehicle.getId(), images);
        if (relativeImagePaths.isEmpty()) {
            throw new IllegalArgumentException("No valid image files could be saved.");
        }

        List<VehicleImage> imageEntities = new ArrayList<>();
        for (String path : relativeImagePaths) {
            VehicleImage img = new VehicleImage();
            img.setVehicle(vehicle);
            img.setFilePath(path);
            img.setFileName(path.substring(path.lastIndexOf("/") + 1));
            imageEntities.add(img);
        }

        vehicleImageRepository.saveAll(imageEntities);
        log.info("Successfully added {} images to vehicle id: {}", imageEntities.size(), vehicleId);

        return relativeImagePaths;
    }

    @Override
    @Cacheable(value = "Vehicles", key = "'allVehicles'")
    public List<VehicleResponseDTO> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle v : vehicleRepository.findAll()) {
            if (v.getStatus() != VehicleStatus.OUT_OF_SERVICE) {
                vehicles.add(v);
            }
        }
        log.info("Vehicles Searched from database");
        return vehicles.stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));

        return vehicleMapper.toDto(vehicle);
    }

    @Override
    public boolean updateVehicle(UpdateVehicleRequestDTO dto) {

        try {
            Optional<Vehicle> vehicle = vehicleRepository.findById(dto.getId());
            if (vehicle.isEmpty()) {
                log.info("Vehicle not present in the Database");
                return false;
            }
            Vehicle vehicle1 = vehicleRepository.getReferenceById(dto.getId());
            vehicle1.setCategory(vehicleCategoryRepository.getVehicleCategoriesById(dto.getCategoryId()));
            vehicle1.setDailyRentalRate(dto.getDailyRentalRate());
            vehicle1.setModel(dto.getModel());
            vehicle1.setUpdatedAt(LocalDateTime.now());

            log.info("Vehicle Updated : Vehicle id " + dto.getId());
            vehicleRepository.save(vehicle1);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean updateOdometer(UpdateOdometerDTO dto) {
        try {
            Vehicle vehicle = vehicleRepository.findById(dto.getId()).orElseThrow();
            vehicle.setCurrentOdometer(dto.getCurrentOdometer());
            log.info("Odometer Updated Successfully");
            vehicleRepository.save(vehicle);
            return true;

        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return false;

    }

    @Override
    public List<VehicleResponseDTO> pendingVehicles() {
        try {
            List<VehicleResponseDTO> response = new ArrayList<>();
            List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.PENDING);
            log.info("Vehicles Pending ");
            return getVehicleResponseDTOS(vehicles, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

    }

    @Transactional
    @CacheEvict(value = "Vehicles", key = "'allVehicles'")
    public VehicleRemoveResponseDTO removePendingVehicles(Long id) {
        try {

            Vehicle v = vehicleRepository.findById(id).orElseThrow();

            VehicleRemoveResponseDTO response = new VehicleRemoveResponseDTO();
            response.setMassage("Vehicle cannot verified Please try again with true verification details");
            response.setLicencePlate(v.getLicensePlate());
            vehicleRepository.delete(v);
            log.info("Vehicle Removed Successfully");
            return response;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Vehicle could not be removed");
        }
    }

    // use an array to get all the list of IDs that are required to make available
    @Override
    @CacheEvict(value = "Vehicles", key = "'allVehicles'")
    public boolean makeAvailable(List<Long> vehicleIdList) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Long id : vehicleIdList) {
            Vehicle v = vehicleRepository.findById(id).orElseThrow();
            vehicles.add(v);
        }
        try {
            for (Vehicle v : vehicles) {
                v.makeAvailable();
                vehicleRepository.save(v);
                log.info(v.getLicensePlate() + " Vehicle is Made to " + v.getStatus().toString() + " State");
            }
            return true;
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<VehicleResponseDTO> filterVehicleByCategory(String category) {

        try {

            VehicleCategory c = vehicleCategoryRepository.findByName(category);
            List<Vehicle> vehicles = c.getVehicles();

            List<VehicleResponseDTO> response = new ArrayList<>();

            return getVehicleResponseDTOS(vehicles, response);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Category Not Found");
        }

    }

    @Override
    public List<VehicleResponseDTO> getVehiclesByFilter(String filterType,
            @Nullable String category,
            @Nullable String status) {

        try {
            switch (filterType) {
                case "category":
                    return filterVehicleByCategory(category);
                case "status":
                    return filterVehicleByStatus(status);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean setVehicleToOutOfService(Long id) {

        try {
            Vehicle v = vehicleRepository.findById(id).orElseThrow();
            v.outOfService();
            vehicleRepository.save(v);
            log.info("Vehicle is Out of Service now");
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<VehicleResponseDTO> filterVehicleByStatus(String status) throws Exception {

        try {
            String s = status.toUpperCase();
            List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.valueOf(s));
            List<VehicleResponseDTO> response = new ArrayList<>();
            return getVehicleResponseDTOS(vehicles, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception("Invalid status Provided");
        }

    }

    private List<VehicleResponseDTO> getVehicleResponseDTOS(List<Vehicle> vehicles, List<VehicleResponseDTO> response) {
        for (Vehicle v : vehicles) {
            VehicleResponseDTO newVehicle = vehicleMapper.toDto(v);
            response.add(newVehicle);
        }

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "Vehicles", key = "'allVehicles'")
    public boolean deleteVehicle(Long id) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id).orElseThrow();
            vehicleImageRepository.deleteByVehicle(vehicle);
            vehicleRepository.deleteById(vehicle.getId());
            fileStorageService.deleteVehicleFiles(vehicle.getId());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), "Vehicle Could not Delete from the system");
            return false;
        }
    }

    public List<VehicleResponseDTO> getVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        List<VehicleResponseDTO> response = new ArrayList<>();
        for (Vehicle v : vehicles) {
            VehicleResponseDTO vehicle = vehicleMapper.toDto(v);
            response.add(vehicle);
        }

        return response;
    }

}
