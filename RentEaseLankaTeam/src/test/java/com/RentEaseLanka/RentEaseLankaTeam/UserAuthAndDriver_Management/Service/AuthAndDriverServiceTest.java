package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model.*;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.DriverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthAndDriverServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthAndDriverService authAndDriverService;

    private User sampleCustomer;
    private User sampleStaff;
    private DriverProfile sampleDriverProfile;

    @BeforeEach
    void setUp() {
        sampleCustomer = new User();
        sampleCustomer.setId(1L);
        sampleCustomer.setFullName("John Doe");
        sampleCustomer.setEmail("john@example.com");
        sampleCustomer.setPassword("hashedPassword123");
        sampleCustomer.setRole(Role.ROLE_CUSTOMER);
        sampleCustomer.setKycVerified(false);

        sampleStaff = new User();
        sampleStaff.setId(2L);
        sampleStaff.setFullName("Staff Member");
        sampleStaff.setEmail("staff@rentease.com");
        sampleStaff.setRole(Role.ROLE_STAFF);

        sampleDriverProfile = new DriverProfile();
        sampleDriverProfile.setId(10L);
        sampleDriverProfile.setUser(sampleCustomer);
        sampleDriverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should register user with BCrypt password and default kycVerified = false")
    void registerUser_Success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("rawPassword123");
        dto.setRole(Role.ROLE_CUSTOMER);

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authAndDriverService.registerUser(dto);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("hashedPassword123", result.getPassword());
        assertFalse(result.getKycVerified());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Staff user should successfully verify customer physical KYC status")
    void verifyPhysicalKYC_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleStaff));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedCustomer = authAndDriverService.verifyPhysicalKYC(1L, 2L);

        assertTrue(updatedCustomer.getKycVerified());
        verify(userRepository, times(1)).save(sampleCustomer);
    }

    @Test
    @DisplayName("Non-staff user should fail physical KYC verification check")
    void verifyPhysicalKYC_AccessDenied() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authAndDriverService.verifyPhysicalKYC(1L, 1L);
        });

        assertEquals("Access Denied: Insufficient permissions to perform KYC verification.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should assign available driver to booking and update status to ON_TRIP")
    void assignDriverToBooking_Success() {
        when(driverRepository.findById(10L)).thenReturn(Optional.of(sampleDriverProfile));
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverProfile updatedDriver = authAndDriverService.assignDriverToBooking(10L, 100L);

        assertEquals(AvailabilityStatus.ON_TRIP, updatedDriver.getAvailabilityStatus());
        verify(driverRepository, times(1)).save(sampleDriverProfile);
    }
}