package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper.BookingDetailsMapper;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverProfileDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.DriverTripDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model.UserSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Repository.DriverBookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.DTOs.CreateUserRequest;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.Role;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private static final Set<BookingStatus> BLOCKING_STATUSES = EnumSet.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.ACTIVE
    );

    private final DriverBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingDetailsMapper bookingDetailsMapper;

    @Override
    public UserSummaryDTO createDriver(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User driver = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DRIVER)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        return UserSummaryDTO.from(userRepository.save(driver));
    }

    @Override
    public List<UserSummaryDTO> getAvailableDrivers(LocalDateTime pickupDateTime, LocalDateTime returnDateTime) {
        return getAvailableDrivers(pickupDateTime, returnDateTime, null);
    }

    @Override
    public List<UserSummaryDTO> getAvailableDrivers(LocalDateTime pickupDateTime, LocalDateTime returnDateTime, Long bookingId) {
        if (pickupDateTime == null || returnDateTime == null || !pickupDateTime.isBefore(returnDateTime)) {
            throw new IllegalArgumentException("Valid pickup and return date-times are required");
        }
        return userRepository.findByRole(Role.DRIVER).stream()
                .filter(driver -> bookingRepository.findOverlappingDriverBookings(
                        driver.getId(), bookingId, pickupDateTime, returnDateTime, BLOCKING_STATUSES).isEmpty())
                .map(UserSummaryDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public BookingDetailsDTO assignDriver(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + driverId));

        if (driver.getRole() != Role.DRIVER) {
            throw new IllegalArgumentException("Selected user is not registered as a driver");
        }
        if (!booking.isWithDriver()) {
            throw new IllegalStateException("This booking does not require a driver");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Driver cannot be assigned to a booking in state " + booking.getStatus());
        }

        List<Booking> overlapping = bookingRepository.findOverlappingDriverBookings(
                driverId, booking.getBookingId(), booking.getPickupDateTime(), booking.getReturnDateTime(), BLOCKING_STATUSES);

        if (!overlapping.isEmpty()) {
            Booking conflict = overlapping.get(0);
            throw new IllegalStateException(String.format(
                    "Driver %s %s is already assigned to Booking #%d from %s to %s.",
                    driver.getFirstName(), driver.getLastName(),
                    conflict.getBookingId(), conflict.getPickupDateTime(), conflict.getReturnDateTime()
            ));
        }

        booking.setDriver(driver);
        bookingRepository.save(booking);
        return bookingDetailsMapper.toDto(booking);
    }

    @Override
    public DriverProfileDTO getDriverProfile(String email) {
        User driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));

        List<Booking> allTrips = bookingRepository.findByDriver_IdOrderByPickupDateTimeAsc(driver.getId());

        long completed = allTrips.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long upcoming = allTrips.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING).count();
        Long activeId = allTrips.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACTIVE)
                .map(Booking::getBookingId)
                .findFirst().orElse(null);

        return DriverProfileDTO.builder()
                .id(driver.getId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(driver.getEmail())
                .role(driver.getRole())
                .verificationStatus(driver.getVerificationStatus())
                .totalCompletedTrips(completed)
                .upcomingTripsCount(upcoming)
                .activeBookingId(activeId)
                .build();
    }

    @Override
    public List<DriverTripDTO> getDriverTrips(String email, BookingStatus status) {
        User driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with email: " + email));

        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByDriver_IdAndStatusInOrderByPickupDateTimeAsc(driver.getId(), Collections.singletonList(status));
        } else {
            bookings = bookingRepository.findByDriver_IdOrderByPickupDateTimeAsc(driver.getId());
        }

        return bookings.stream().map(this::mapToTripDto).toList();
    }

    @Override
    public List<DriverTripDTO> getAssignedBookingsForDriver(Long driverId) {
        if (!userRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found with ID: " + driverId);
        }
        return bookingRepository.findByDriver_IdOrderByPickupDateTimeAsc(driverId)
                .stream()
                .map(this::mapToTripDto)
                .toList();
    }

    private DriverTripDTO mapToTripDto(Booking b) {
        String customerName = b.getUser() != null
                ? ((b.getUser().getFirstName() != null ? b.getUser().getFirstName() : "") + " "
                + (b.getUser().getLastName() != null ? b.getUser().getLastName() : "")).trim()
                : "Customer";

        return DriverTripDTO.builder()
                .bookingId(b.getBookingId())
                .status(b.getStatus())
                .pickupDateTime(b.getPickupDateTime())
                .returnDateTime(b.getReturnDateTime())
                .pickupLocation(b.getPickupLocation())
                .returnLocation(b.getReturnLocation())
                .totalAmount(b.getTotalAmount())
                .vehicleId(b.getVehicle() != null ? b.getVehicle().getId() : null)
                .vehicleModel(b.getVehicle() != null ? b.getVehicle().getModel() : null)
                .vehiclePlate(b.getVehicle() != null ? b.getVehicle().getLicensePlate() : null)
                .customerId(b.getUser() != null ? b.getUser().getId() : null)
                .customerName(customerName)
                .customerEmail(b.getUser() != null ? b.getUser().getEmail() : null)
                .build();
    }
}
