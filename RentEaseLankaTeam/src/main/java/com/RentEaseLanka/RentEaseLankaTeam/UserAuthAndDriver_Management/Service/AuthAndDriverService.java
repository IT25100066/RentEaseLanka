package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.*;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.DriverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthAndDriverService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    // 1. User Registration Logic
    public User registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email address is already registered");
        }
        User user = new User(dto.getFullName(), dto.getEmail(), dto.getPassword(), dto.getRole());
        return userRepository.save(user);
    }

    // 2. Physical KYC Verification (Staff Only)
    public User verifyPhysicalKYC(Long userId, Long staffUserId) {
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new RuntimeException("Staff account not found"));

        if (staff.getRole() != Role.ROLE_STAFF && staff.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access Denied: Insufficient staff permissions");
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setKycVerified(true);
        return userRepository.save(customer);
    }

    // 3. Create Driver Profile
    public DriverProfile createDriverProfile(DriverRegistrationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User account not found"));

        DriverProfile driver = new DriverProfile();
        driver.setUser(user);
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseCategory(dto.getLicenseCategory());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setExperienceYears(dto.getExperienceYears());
        driver.setDailyRate(dto.getDailyRate());
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        return driverRepository.save(driver);
    }

    // 4. Fetch Available Drivers
    public List<DriverProfile> getAvailableDrivers() {
        return driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    // 5. Update Driver Availability Status
    @Transactional
    public DriverProfile updateDriverStatus(Long driverId, AvailabilityStatus status) {
        DriverProfile driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setAvailabilityStatus(status);
        return driverRepository.save(driver);
    }
}