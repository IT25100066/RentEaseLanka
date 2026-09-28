package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Service.FileStorageService;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("fleetFileStorageService")
public class FileStorageService {
    private final Path rootLocation = Paths.get("uploads/vehicles");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Init failed");
        }
    }

    public List<String> storeFiles(Long vehicleId, List<MultipartFile> files) {
        Path vehicleDir = this.rootLocation.resolve("vehicle_" + vehicleId);
        try {
            Files.createDirectories(vehicleDir);
        } catch (IOException e) {
            throw new RuntimeException("Create directory failed");
        }
        List<String> storedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + "." + ext;
            try {
                Files.copy(file.getInputStream(), vehicleDir.resolve(newFileName));
                storedPaths.add("/uploads/vehicles/vehicle_" + vehicleId + "/" +
                        newFileName);
            } catch (IOException e) {
                throw new RuntimeException("Upload file failed");
            }
        }
        return storedPaths;
    }

    public boolean deleteVehicleFiles(Long vehicleId) {
        Path vehicleDir = this.rootLocation.resolve("vehicle_" + vehicleId);
        try {
            return FileSystemUtils.deleteRecursively(vehicleDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete vehicle directory", e);
        }
    }

}
