package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Controller;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service.BookingService;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.VerificationStatus;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
                return new ResponseEntity<>("User is not verified. Please upload NIC to get verified.",
                        HttpStatus.FORBIDDEN);
            }

            BookingDetailsDTO createdBooking = bookingService.createBooking(bookingRequestDTO, user);
            Map<String, Object> response = new HashMap<>();
            response.put("booking", createdBooking);
            log.info("Booking created successfully", response);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create booking", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculateTotalPrice(@RequestBody BookingRequestDTO bookingRequestDTO) {
        try {
            BigDecimal totalPrice = bookingService.calculateTotalPrice(bookingRequestDTO);
            return new ResponseEntity<>(totalPrice, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to calculate total price", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            boolean cancelled = bookingService.cancelBooking(id);
            if (cancelled) {
                return new ResponseEntity<>("Booking Cancelled Successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("Could not cancel booking", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Failed to cancel booking", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<BookingDetailsDTO> bookings = bookingService.getAllBookings();
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch all bookings", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> viewBookingDetails(@PathVariable Long id) {
        try {
            BookingDetailsDTO bookingDetails = bookingService.viewBookingDetails(id);
            return new ResponseEntity<>(bookingDetails, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch booking details", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        try {
            boolean confirmed = bookingService.makeConfirmed(id);
            if (confirmed) {
                return new ResponseEntity<>("Booking Confirmed Successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("Could not confirm booking", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Failed to confirm booking", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> activateBooking(@PathVariable Long id) {
        try {
            boolean active = bookingService.makeActive(id);
            if (active) {
                return new ResponseEntity<>("Booking Activated Successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("Could not activate booking", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Failed to activate booking", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            boolean completed = bookingService.makeCompleted(id);
            if (completed) {
                return new ResponseEntity<>("Booking Completed Successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("Could not complete booking", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Failed to complete booking", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getBookingForCustomer(@AuthenticationPrincipal User currentUser) {
        try {
            String email = currentUser.getEmail();
            List<BookingDetailsDTO> bookings = bookingService.getBookingForCustomer(email);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch bookings for customer", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getBookingsForCustomer(@PathVariable Long customerId) {
        try {
            List<BookingDetailsDTO> bookings = bookingService.getBookingsForCustomer(customerId);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to find bookings for customer {}: {}", customerId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Failed to fetch bookings for customer {}", customerId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
