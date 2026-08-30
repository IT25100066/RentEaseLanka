package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.*;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.DriverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.UserRepository;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class handling core business logic for Authentication, User Profile Management,
 * Staff Physical KYC Verification, and Driver Availability/Status updates.
 */
@Service
public class AuthAndDriverService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    /**
     * Registers a new user account (Customer, Driver, Staff, or Admin).
     * Hashes the raw password using BCrypt and initializes kycVerified to false by default.
     */
    public User registerUser(UserRegisterDTO dto) {
        // 1. Verify that the email is not already registered in the system
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email address is already registered.");
        }

        // 2. Build and populate the new User entity
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        // Encrypt the plain text password securely via BCrypt hashing algorithm
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        // According to functional spec, kycVerified defaults to false until physical counter check
        user.setKycVerified(false);

        // 3. Save and return the persisted user record
        return userRepository.save(user);
    }

    /**
     * Authenticates user credentials and generates a JWT access token upon success.
     */
    public AuthResponseDTO login(LoginDTO loginDTO) {
        // 1. Locate user record by email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginDTO.getEmail()));

        // 2. Compare incoming raw password against the encrypted hash in the database
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // 3. Generate signed JWT token including email, role, and userId claims
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // 4. Return response payload wrapping token and user metadata
        return new AuthResponseDTO(token, user.getEmail(), user.getRole().name(), user.getId());
    }

    /**
     * Creates an onboarding DriverProfile record linked to a User account with ROLE_DRIVER.
     */
    public DriverProfile createDriverProfile(DriverRegistrationDTO dto) {
        // 1. Fetch associated user account from database
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User account not found with ID: " + dto.getUserId()));

        // 2. Validate that the user account holds the DRIVER role
        if (user.getRole() != Role.ROLE_DRIVER) {
            throw new RuntimeException("User account is not assigned the ROLE_DRIVER role.");
        }

        // 3. Create driver profile entity with document details and set initial availability to AVAILABLE
        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        profile.setLicenseNumber(dto.getLicenseNumber());
        profile.setNicNumber(dto.getNicNumber());
        profile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        return driverRepository.save(profile);
    }

    /**
     * Staff/Admin physical document counter check verification.
     * Updates customer's kycVerified status to true after validating physical NIC/Passport and License.
     */
    public User verifyPhysicalKYC(Long userId, Long staffUserId) {
        // 1. Fetch executing staff member record to verify permissions
        User staffUser = userRepository.findById(staffUserId)
                .orElseThrow(() -> new RuntimeException("Staff user not found with ID: " + staffUserId));

        // 2. Guard clause: Ensure executing user has ROLE_STAFF or ROLE_ADMIN privileges
        if (staffUser.getRole() != Role.ROLE_STAFF && staffUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access Denied: Insufficient permissions to perform KYC verification.");
        }

        // 3. Fetch target customer profile by ID
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer account not found with ID: " + userId));

        // 4. Set KYC verification flag to true after physical counter check pass
        customer.setKycVerified(true);

        return userRepository.save(customer);
    }

    /**
     * Assigns an available driver to a specific reservation booking.
     * Transitions driver availability status to ON_TRIP.
     */
    public DriverProfile assignDriverToBooking(Long driverId, Long bookingId) {
        // 1. Fetch driver profile by ID
        DriverProfile profile = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found with ID: " + driverId));

        // 2. Validate that driver is currently AVAILABLE
        if (profile.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new RuntimeException("Selected driver is not available for assignment.");
        }

        // 3. Update status to ON_TRIP
        profile.setAvailabilityStatus(AvailabilityStatus.ON_TRIP);

        // 4. Save and return updated driver profile
        return driverRepository.save(profile);
    }

    /**
     * Retrieves all active driver profiles currently marked with AVAILABLE status.
     */
    public List<DriverProfile> getAvailableDrivers() {
        return driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    /**
     * Updates a driver's availability state (e.g., AVAILABLE, ON_TRIP, OFF_DUTY, SUSPENDED).
     */
    public DriverProfile updateDriverStatus(Long driverId, AvailabilityStatus status) {
        DriverProfile profile = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found with ID: " + driverId));

        profile.setAvailabilityStatus(status);
        return driverRepository.save(profile);
    }

    /**
     * Driver Duty Portal: Updates assigned trip progress (e.g., STARTED -> COMPLETED).
     * Automatically resets driver availabilityStatus to AVAILABLE when the trip is marked COMPLETED.
     */
    public DriverProfile updateTripStatus(Long driverId, String newStatus) {
        // 1. Fetch driver profile by ID
        DriverProfile driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found with ID: " + driverId));

        // 2. Transition availability status based on trip progress
        if ("COMPLETED".equalsIgnoreCase(newStatus)) {
            // Auto-release driver availability upon trip completion
            driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        } else if ("STARTED".equalsIgnoreCase(newStatus)) {
            driver.setAvailabilityStatus(AvailabilityStatus.ON_TRIP);
        }

        // 3. Save and return updated driver profile state
        return driverRepository.save(driver);
    }

}