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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DriverService driverService;

    private DriverProfile sampleDriverProfile;

    @BeforeEach
    void setUp() {
        sampleDriverProfile = new DriverProfile();
        sampleDriverProfile.setId(10L);
        sampleDriverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should assign available driver to booking and update status to ON_TRIP")
    void assignDriverToBooking_Success() {
        when(driverRepository.findById(10L)).thenReturn(Optional.of(sampleDriverProfile));
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverProfile updatedDriver = driverService.assignDriverToBooking(10L, 100L);

        assertEquals(AvailabilityStatus.ON_TRIP, updatedDriver.getAvailabilityStatus());
        verify(driverRepository, times(1)).save(sampleDriverProfile);
    }
}