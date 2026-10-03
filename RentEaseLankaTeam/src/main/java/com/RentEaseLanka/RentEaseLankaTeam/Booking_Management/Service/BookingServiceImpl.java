<<<<<<< HEAD
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper.BookingDetailsMapper;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Repository.HandoverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto.CreateInvoiceRequest;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentType;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.InvoiceRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.PaymentRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service.PaymentService;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Repository.VehicleReturnInspectionRepository;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingDetailsMapper bookingDetailsMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HandoverRepository handoverRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VehicleReturnInspectionRepository returnInspectionRepository;

    @Override
    public boolean checkVehicleAvailability(Vehicle vehicle) {
        return vehicle.getStatus() != VehicleStatus.OUT_OF_SERVICE
                && vehicle.getStatus() != VehicleStatus.UNDER_MAINTENANCE
                && vehicle.getStatus() != VehicleStatus.PENDING;
    }

    @Override
    public boolean checkVehicleAvailabilityForBooking(BookingRequestDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));

        if (!checkVehicleAvailability(vehicle)) {
            log.info("Vehicle {} is not available (Status: {})", vehicle.getId(), vehicle.getStatus());
            return false;
        }

        List<Booking> bookings = bookingRepository.findByVehicle(vehicle);

        for (Booking booking : bookings) {
            // Exclude cancelled and completed bookings from overlap check
            if (booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.COMPLETED) {
                if (dto.getPickupDateTime().isBefore(booking.getReturnDateTime())
                        && dto.getReturnDateTime().isAfter(booking.getPickupDateTime())) {
                    log.info("Vehicle {} is already booked from {} to {} (Booking #{} status: {})",
                            vehicle.getId(), booking.getPickupDateTime(), booking.getReturnDateTime(),
                            booking.getBookingId(), booking.getStatus());
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateDates(BookingRequestDTO dto) {
        return dto.getPickupDateTime() != null
                && dto.getReturnDateTime() != null
                && dto.getPickupDateTime().isBefore(dto.getReturnDateTime());
    }

    @Transactional
    @Override
    public BookingDetailsDTO createBooking(BookingRequestDTO dto, User user) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));

        if (!validateDates(dto)) {
            throw new IllegalArgumentException("Invalid Dates: Pickup date must be before return date.");
        }

        // Allow 5 minutes buffer for form submission time drift
        if (dto.getPickupDateTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            log.error("Invalid pickup date: Pickup date cannot be in the past.");
            throw new IllegalArgumentException("Invalid pickup date: Pickup date cannot be in the past.");
        }

        if (!checkVehicleAvailabilityForBooking(dto)) {
            log.error("Vehicle is not available for booking during this period");
            throw new IllegalStateException("Vehicle is not available for booking during the selected dates.");
        }

        User bookedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal totalPrice = calculateTotalPrice(dto);

        Booking booking = new Booking();
        booking.setVehicle(vehicle);
        booking.setPickupDateTime(dto.getPickupDateTime());
        booking.setReturnDateTime(dto.getReturnDateTime());
        booking.setPickupLocation(dto.getPickupLocation());
        booking.setReturnLocation(dto.getReturnLocation());
        booking.setUser(bookedUser);
        booking.setTotalAmount(totalPrice);
        booking.setWithDriver(dto.isWithDriver());

        if (vehicle.getStatus() == VehicleStatus.AVAILABLE) {
            try {
                vehicle.reserve();
                vehicleRepository.save(vehicle);
            } catch (Exception e) {
                log.warn("Vehicle state could not transition to reserved: {}", e.getMessage());
            }
        }

        Booking savedBooking = bookingRepository.save(booking);

        try {
            long days = Math
                    .abs(ChronoUnit.DAYS.between(savedBooking.getPickupDateTime(), savedBooking.getReturnDateTime()));
            if (days == 0)
                days = 1;

            CreateInvoiceRequest invoiceReq = new CreateInvoiceRequest();
            invoiceReq.vehicleReservationId = savedBooking.getBookingId();
            invoiceReq.customerId = bookedUser.getId();
            invoiceReq.rentalDays = (int) days;
            invoiceReq.dailyRate = vehicle.getDailyRentalRate() != null ? vehicle.getDailyRentalRate()
                    : BigDecimal.valueOf(5000);
            invoiceReq.subtotal = savedBooking.getTotalAmount();
            invoiceReq.taxAmount = BigDecimal.ZERO.setScale(2);
            invoiceReq.pickupDeadline = savedBooking.getPickupDateTime().plusHours(2);

            paymentService.createInvoice(invoiceReq);
            log.info("Generated Invoice for Booking #{}", savedBooking.getBookingId());
        } catch (Exception e) {
            log.warn("Could not auto-generate invoice for booking #{}: {}", savedBooking.getBookingId(),
                    e.getMessage());
        }

        return bookingDetailsMapper.toDto(savedBooking);
    }

    @Override
    public BigDecimal calculateTotalPrice(BookingRequestDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));
        long days = Math.abs(ChronoUnit.DAYS.between(dto.getPickupDateTime(), dto.getReturnDateTime()));
        if (days == 0)
            days = 1; // Minimum 1 day calculation
        BigDecimal dailyRate = vehicle.getDailyRentalRate() != null ? vehicle.getDailyRentalRate()
                : BigDecimal.valueOf(5000);
        if (dto.isWithDriver()) {
            Booking.calculateDriverFee(days);
            return dailyRate.multiply(BigDecimal.valueOf(days))
                    .add(BigDecimal.valueOf(Booking.getDriverFee()));
        }
        return dailyRate.multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    @Override
    public boolean cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Booking cannot be cancelled in its current state (" + booking.getStatus() + ").");
        }

        Vehicle vehicle = booking.getVehicle();

        booking.makeCancelled();
        if (vehicle != null && vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            try {
                vehicle.makeAvailable();
            } catch (Exception e) {
                // If not in a state that transitions to available, log warning
                log.warn("Vehicle state could not transition to available: {}", e.getMessage());
            }
            vehicleRepository.save(vehicle);
        }

        bookingRepository.save(booking);
        return true;
    }

    @Override
    public BookingDetailsDTO viewBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));
        return bookingDetailsMapper.toDto(booking);
    }

    @Transactional
    @Override
    public boolean makeConfirmed(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Booking cannot be confirmed in its current state (" + booking.getStatus() + ").");
        }

        booking.makeConfirmed();
        bookingRepository.save(booking);
        return true;
    }

    /**
     * Booking activation requires a completed Handover record.
     * Vehicle status is only changed to RENTED via the Handover workflow.
     */
    @Transactional
    @Override
    public boolean makeActive(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Booking cannot be activated in its current state (" + booking.getStatus() + ").");
        }

        // Strict Enforcement: Vehicle cannot be rented or booking activated without
        // completing handover
        boolean handoverCompleted = handoverRepository.findByBookingId(bookingId).stream()
                .anyMatch(h -> h.getStatus() == HandoverStatus.COMPLETED);

        if (!handoverCompleted) {
            log.error("Validation Error: Cannot activate booking #{}. Vehicle handover has not been completed.",
                    bookingId);
            throw new IllegalStateException("Cannot rent vehicle or activate Booking #" + bookingId
                    + ": Vehicle handover has not been completed. Please complete the handover steps (Starting mileage, Damage inspection) in Handover Management first.");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            booking.makeActive();
            bookingRepository.save(booking);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean makeCompleted(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Booking cannot be completed in its current state (" + booking.getStatus() + ").");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only a Active booking can be completed.");
        }

        var invoice = invoiceRepository.findByVehicleReservationId(bookingId)
                .orElseThrow(
                        () -> new IllegalStateException("Booking cannot be completed without its rental invoice."));
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new IllegalStateException(
                    "Booking cannot be completed until the customer has paid the full rental amount.");
        }
        if (returnInspectionRepository.findByBookingId(bookingId).isEmpty()) {
            throw new IllegalStateException(
                    "Booking cannot be completed until the vehicle return inspection is recorded.");
        }
        boolean activeDepositExists = paymentRepository
                .findFirstByInvoiceIdAndPaymentTypeAndStatus(invoice.getId(), PaymentType.SECURITY_DEPOSIT_HOLD,
                        PaymentStatus.HELD)
                .isPresent();
        if (activeDepositExists) {
            throw new IllegalStateException(
                    "Booking cannot be completed until the security deposit is released or used for recorded damage.");
        }

        Vehicle vehicle = booking.getVehicle();
        if (vehicle != null) {
            try {
                vehicle.returnVehicle();
            } catch (Exception e) {
                log.warn("Vehicle return transition warning: {}", e.getMessage());
            }
            vehicleRepository.save(vehicle);
        }

        booking.makeCompleted();
        bookingRepository.save(booking);
        return true;
    }

    @Override
    public List<BookingDetailsDTO> getBookingForCustomer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        List<Booking> bookings = bookingRepository.findByUser(user);

        if (bookings.isEmpty()) {
            return List.of();
        }
        return bookings.stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetailsDTO> getBookingsForCustomer(Long customerId) {
        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found with ID: " + customerId);
        }

        User user = userRepository.findById(customerId).orElse(null);

        return bookingRepository.findByUser(user).stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetailsDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

}
=======
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper.BookingDetailsMapper;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Repository.HandoverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto.CreateInvoiceRequest;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentType;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.InvoiceRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.PaymentRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service.PaymentService;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Repository.VehicleReturnInspectionRepository;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingDetailsMapper bookingDetailsMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HandoverRepository handoverRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VehicleReturnInspectionRepository returnInspectionRepository;


    @Override
    public boolean checkVehicleAvailability(Vehicle vehicle) {
        return vehicle.getStatus() != VehicleStatus.OUT_OF_SERVICE
                && vehicle.getStatus() != VehicleStatus.UNDER_MAINTENANCE;
    }

    @Override
    public boolean checkVehicleAvailabilityForBooking(BookingRequestDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));

        if (!checkVehicleAvailability(vehicle)) {
            log.info("Vehicle {} is not available (Status: {})", vehicle.getId(), vehicle.getStatus());
            return false;
        }

        List<Booking> bookings = bookingRepository.findByVehicle(vehicle);

        for (Booking booking : bookings) {
            // Exclude cancelled bookings from overlap check
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                if (dto.getPickupDateTime().isBefore(booking.getReturnDateTime())
                        && dto.getReturnDateTime().isAfter(booking.getPickupDateTime())) {
                    log.info("Vehicle {} is already booked from {} to {} (Booking #{} status: {})",
                            vehicle.getId(), booking.getPickupDateTime(), booking.getReturnDateTime(),
                            booking.getBookingId(), booking.getStatus());
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateDates(BookingRequestDTO dto) {
        return dto.getPickupDateTime().isBefore(dto.getReturnDateTime());
    }

    @Transactional
    @Override
    public BookingDetailsDTO createBooking(BookingRequestDTO dto, User user) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));

        if (!validateDates(dto)) {
            throw new IllegalArgumentException("Invalid Dates: Pickup date must be before return date.");
        }

        if (!checkVehicleAvailabilityForBooking(dto)) {
            log.error("Vehicle is not available for booking during this period");
            throw new IllegalStateException("Vehicle is not available for booking during the selected dates.");
        }

        User bookedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal totalPrice = calculateTotalPrice(dto);

        Booking booking = new Booking();
        booking.setVehicle(vehicle);
        booking.setPickupDateTime(dto.getPickupDateTime());
        booking.setReturnDateTime(dto.getReturnDateTime());
        booking.setPickupLocation(dto.getPickupLocation());
        booking.setReturnLocation(dto.getReturnLocation());
        booking.setUser(bookedUser);
        booking.setTotalAmount(totalPrice);
        booking.setWithDriver(dto.isWithDriver());

        if (dto.getPickupDateTime().isBefore(LocalDateTime.now())) {
            log.error("Invalid pickup date: Pickup date cannot be in the past.");
            throw new IllegalArgumentException("Invalid pickup date: Pickup date cannot be in the past.");
        }

        if (vehicle.getStatus() != VehicleStatus.RESERVED) {
            vehicle.reserve();
            vehicleRepository.save(vehicle);
        }

        Booking savedBooking = bookingRepository.save(booking);

        try {
            long days = Math.abs(ChronoUnit.DAYS.between(savedBooking.getPickupDateTime(), savedBooking.getReturnDateTime()));
            if (days == 0) days = 1;

            CreateInvoiceRequest invoiceReq = new CreateInvoiceRequest();
            invoiceReq.vehicleReservationId = savedBooking.getBookingId();
            invoiceReq.customerId = bookedUser.getId();
            invoiceReq.rentalDays = (int) days;
            invoiceReq.dailyRate = vehicle.getDailyRentalRate() != null ? vehicle.getDailyRentalRate() : BigDecimal.valueOf(5000);
            invoiceReq.subtotal = savedBooking.getTotalAmount();
            invoiceReq.taxAmount = BigDecimal.ZERO.setScale(2);
            invoiceReq.pickupDeadline = savedBooking.getPickupDateTime().plusHours(2);

            paymentService.createInvoice(invoiceReq);
            log.info("Generated Invoice for Booking #{}", savedBooking.getBookingId());
        } catch (Exception e) {
            log.warn("Could not auto-generate invoice for booking #{}: {}", savedBooking.getBookingId(), e.getMessage());
        }

        return bookingDetailsMapper.toDto(savedBooking);
    }

    @Override
    public BigDecimal calculateTotalPrice(BookingRequestDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));
        long days = Math.abs(ChronoUnit.DAYS.between(dto.getPickupDateTime(), dto.getReturnDateTime()));
        if (days == 0) days = 1; // Minimum 1 day calculation
        if (dto.isWithDriver()) {
            Booking.calculateDriverFee(days);
            return vehicle.getDailyRentalRate().multiply(BigDecimal.valueOf(days))
                    .add(BigDecimal.valueOf(Booking.getDriverFee()));
        }
        return vehicle.getDailyRentalRate().multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    @Override
    public boolean cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking cannot be cancelled in its current state (" + booking.getStatus() + ").");
        }

        Vehicle vehicle = booking.getVehicle();

        booking.makeCancelled();
        if (vehicle != null && vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            try {
                vehicle.makeAvailable();
            } catch (Exception e) {
                // If not in a state that transitions to available, log warning
                log.warn("Vehicle state could not transition to available: {}", e.getMessage());
            }
            vehicleRepository.save(vehicle);
        }

        bookingRepository.save(booking);
        return true;
    }

    @Override
    public BookingDetailsDTO viewBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));
        return bookingDetailsMapper.toDto(booking);
    }

    @Transactional
    @Override
    public boolean makeConfirmed(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking cannot be confirmed in its current state (" + booking.getStatus() + ").");
        }

        booking.makeConfirmed();
        bookingRepository.save(booking);
        return true;
    }

    /**
     * Booking activation requires a completed Handover record.
     * Vehicle status is only changed to RENTED via the Handover workflow.
     */
    @Transactional
    @Override
    public boolean makeActive(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking cannot be activated in its current state (" + booking.getStatus() + ").");
        }

        // Strict Enforcement: Vehicle cannot be rented or booking activated without completing handover
        boolean handoverCompleted = handoverRepository.findByBookingId(bookingId).stream()
                .anyMatch(h -> h.getStatus() == HandoverStatus.COMPLETED);

        if (!handoverCompleted) {
            log.error("Validation Error: Cannot activate booking #{}. Vehicle handover has not been completed.", bookingId);
            throw new IllegalStateException("Cannot rent vehicle or activate Booking #" + bookingId
                    + ": Vehicle handover has not been completed. Please complete the handover steps (Starting mileage, Damage inspection) in Handover Management first.");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            booking.makeActive();
            bookingRepository.save(booking);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean makeCompleted(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking cannot be completed in its current state (" + booking.getStatus() + ").");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only a Active booking can be completed.");
        }

        var invoice = invoiceRepository.findByVehicleReservationId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking cannot be completed without its rental invoice."));
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new IllegalStateException("Booking cannot be completed until the customer has paid the full rental amount.");
        }
        if (returnInspectionRepository.findByBookingId(bookingId).isEmpty()) {
            throw new IllegalStateException("Booking cannot be completed until the vehicle return inspection is recorded.");
        }
        boolean activeDepositExists = paymentRepository
                .findFirstByInvoiceIdAndPaymentTypeAndStatus(invoice.getId(), PaymentType.SECURITY_DEPOSIT_HOLD, PaymentStatus.HELD)
                .isPresent();
        if (activeDepositExists) {
            throw new IllegalStateException("Booking cannot be completed until the security deposit is released or used for recorded damage.");
        }

        Vehicle vehicle = booking.getVehicle();
        if (vehicle != null) {
            try {
                vehicle.returnVehicle();
            } catch (Exception e) {
                log.warn("Vehicle return transition warning: {}", e.getMessage());
            }
            vehicleRepository.save(vehicle);
        }

        booking.makeCompleted();
        bookingRepository.save(booking);
        return true;
    }

    @Override
    public List<BookingDetailsDTO> getBookingForCustomer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        List<Booking> bookings = bookingRepository.findByUser(user);

        if (bookings.isEmpty()) {
            return List.of();
        }
        return bookings.stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetailsDTO> getBookingsForCustomer(Long customerId) {
        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found with ID: " + customerId);
        }

        User user = userRepository.findById(customerId).orElse(null);
                

        return bookingRepository.findByUser(user).stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetailsDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingDetailsMapper::toDto)
                .collect(Collectors.toList());
    }

}
>>>>>>> 21bee750d2eb336b0bafde9dc94de064033fd540
