package com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.VehicleStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.AvailableState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle_State.RentedState;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.DTO.*;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverRecord;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Model.HandoverStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Handover_Management.Repository.HandoverRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service.PaymentService;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandoverServiceImpl implements HandoverService {

    private final HandoverRepository handoverRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public HandoverResponseDTO initiateHandover(HandoverCreateRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Handover request payload cannot be empty.");
        }

        if (dto.getStaffId() == null || dto.getStaffId().trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID is required to initiate a handover.");
        }

        // 1. Resolve Booking if bookingId is provided
        Booking associatedBooking = null;
        if (dto.getBookingId() != null) {
            associatedBooking = bookingRepository.findById(dto.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: #" + dto.getBookingId()));

            // Validate booking state
            if (associatedBooking.getStatus() == BookingStatus.CANCELLED) {
                throw new IllegalStateException("Cannot initiate handover: Booking #" + dto.getBookingId() + " is CANCELLED.");
            }
            if (associatedBooking.getStatus() == BookingStatus.COMPLETED) {
                throw new IllegalStateException("Cannot initiate handover: Booking #" + dto.getBookingId() + " is already COMPLETED.");
            }
            if (associatedBooking.getStatus() == BookingStatus.ACTIVE) {
                throw new IllegalStateException("Cannot initiate handover: Booking #" + dto.getBookingId() + " is already ACTIVE (handover already done).");
            }

            // Auto-deduce vehicleId if not explicitly provided
            if (dto.getVehicleId() == null) {
                if (associatedBooking.getVehicle() == null) {
                    throw new IllegalStateException("Booking #" + dto.getBookingId() + " does not have an assigned vehicle.");
                }
                dto.setVehicleId(associatedBooking.getVehicle().getId());
            } else if (associatedBooking.getVehicle() != null && !associatedBooking.getVehicle().getId().equals(dto.getVehicleId())) {
                throw new IllegalArgumentException("Vehicle ID mismatch: Booking #" + dto.getBookingId()
                        + " is for vehicle ID " + associatedBooking.getVehicle().getId()
                        + " (" + associatedBooking.getVehicle().getLicensePlate() + "), not vehicle ID " + dto.getVehicleId());
            }

            // Check if booking already has a non-cancelled handover record
            boolean bookingHasActiveHandover = handoverRepository.existsByBookingIdAndStatusIn(
                    dto.getBookingId(),
                    Arrays.asList(HandoverStatus.PENDING, HandoverStatus.IN_PROGRESS, HandoverStatus.COMPLETED)
            );
            if (bookingHasActiveHandover) {
                throw new IllegalStateException("Booking #" + dto.getBookingId() + " already has an active or completed handover record.");
            }

            // Auto-populate Customer ID from booking if not provided
            if (dto.getCustomerId() == null && associatedBooking.getUser() != null) {
                dto.setCustomerId(associatedBooking.getUser().getId());
            }

            // Auto-populate Customer Name from booking if not provided
            if ((dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()) && associatedBooking.getUser() != null) {
                User bookedUser = associatedBooking.getUser();
                String name = ((bookedUser.getFirstName() != null ? bookedUser.getFirstName() : "") + " "
                        + (bookedUser.getLastName() != null ? bookedUser.getLastName() : "")).trim();
                if (name.isEmpty()) {
                    name = bookedUser.getEmail();
                }
                dto.setCustomerName(name);
            }

        }

        // 2. Resolve Vehicle
        if (dto.getVehicleId() == null) {
            throw new IllegalArgumentException("Vehicle ID is required to initiate handover.");
        }

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + dto.getVehicleId()));

        // Validate vehicle status
        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new IllegalStateException("Cannot initiate handover: Vehicle [" + vehicle.getLicensePlate()
                    + "] is currently RENTED. Return the vehicle before starting a new handover.");
        }
        if (vehicle.getStatus() == VehicleStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Cannot initiate handover: Vehicle [" + vehicle.getLicensePlate()
                    + "] is currently UNDER MAINTENANCE.");
        }
        if (vehicle.getStatus() == VehicleStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Cannot initiate handover: Vehicle [" + vehicle.getLicensePlate()
                    + "] is OUT OF SERVICE.");
        }

        // 3. Validate that vehicle does not already have an active handover in progress
        boolean hasActiveHandover = handoverRepository.existsByVehicleAndStatusIn(
                vehicle,
                Arrays.asList(HandoverStatus.PENDING, HandoverStatus.IN_PROGRESS)
        );
        if (hasActiveHandover) {
            log.error("Validation Error: Vehicle {} already has an active handover in progress.", vehicle.getLicensePlate());
            throw new IllegalStateException("Vehicle [" + vehicle.getLicensePlate() + "] already has an active handover in progress.");
        }

        // 4. Auto-resolve Customer Name if customerId was provided without booking
        if (dto.getCustomerId() != null && (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty())) {
            userRepository.findById(dto.getCustomerId()).ifPresent(u -> {
                String name = ((u.getFirstName() != null ? u.getFirstName() : "") + " "
                        + (u.getLastName() != null ? u.getLastName() : "")).trim();
                if (name.isEmpty()) {
                    name = u.getEmail();
                }
                dto.setCustomerName(name);
            });
        }

        // 5. Create Handover Record
        HandoverRecord record = HandoverRecord.builder()
                .vehicle(vehicle)
                .bookingId(dto.getBookingId())
                .customerId(dto.getCustomerId())
                .customerName(dto.getCustomerName() != null ? dto.getCustomerName().trim() : null)
                .staffId(dto.getStaffId().trim())
                .status(HandoverStatus.PENDING)
                .hasDamage(false)
                .paymentVerified(false)
                .notes(dto.getInitialNotes() != null ? dto.getInitialNotes().trim() : null)
                .handoverDateTime(LocalDateTime.now())
                .build();

        HandoverRecord savedRecord = handoverRepository.save(record);
        log.info("Handover initiated successfully with ID: {} for vehicle: {}", savedRecord.getId(), vehicle.getLicensePlate());

        return mapToResponseDTO(savedRecord);
    }

    @Override
    public List<HandoverResponseDTO> getAllHandovers(String status) {
        List<HandoverRecord> records;
        if (status != null && !status.trim().isEmpty()) {
            try {
                HandoverStatus parsedStatus = HandoverStatus.valueOf(status.toUpperCase().trim());
                records = handoverRepository.findByStatus(parsedStatus);
            } catch (IllegalArgumentException e) {
                log.error("Invalid HandoverStatus filter: {}", status);
                throw new IllegalArgumentException("Invalid status filter: '" + status + "'. Valid options are: PENDING, IN_PROGRESS, COMPLETED, CANCELLED.");
            }
        } else {
            records = handoverRepository.findAll();
        }

        return records.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HandoverResponseDTO getHandoverById(Long id) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));
        return mapToResponseDTO(record);
    }

    @Override
    public List<HandoverResponseDTO> getHandoversByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + vehicleId);
        }
        return handoverRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HandoverResponseDTO updateHandover(Long id, HandoverUpdateRequestDTO dto) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED || record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a handover record that is already " + record.getStatus() + ".");
        }

        if (dto.getStaffId() != null && !dto.getStaffId().trim().isEmpty()) {
            record.setStaffId(dto.getStaffId().trim());
        }
        if (dto.getCustomerName() != null) {
            record.setCustomerName(dto.getCustomerName().trim());
        }
        if (dto.getFuelLevel() != null) {
            record.setFuelLevel(dto.getFuelLevel().trim());
        }
        if (dto.getNotes() != null) {
            record.setNotes(dto.getNotes().trim());
        }
        if (dto.getDamageDescription() != null) {
            record.setDamageDescription(dto.getDamageDescription().trim());
        }

        HandoverRecord updated = handoverRepository.save(record);
        log.info("Handover record ID: {} updated successfully.", id);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public boolean deleteHandover(Long id) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED) {
            log.error("Validation Error: Cannot delete a COMPLETED handover record (ID: {}).", id);
            throw new IllegalStateException("Cannot delete a completed handover record (ID: #" + id + ").");
        }

        handoverRepository.delete(record);
        log.info("Handover record ID: {} deleted successfully.", id);
        return true;
    }



    @Override
    @Transactional
    public HandoverResponseDTO recordMileage(Long id, MileageRecordDTO dto) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED || record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record mileage for a handover that is already " + record.getStatus() + ".");
        }

        if (dto.getStartingMileage() == null || dto.getStartingMileage() < 0) {
            throw new IllegalArgumentException("Starting mileage must be a valid non-negative number.");
        }

        double vehicleCurrentOdometer = record.getVehicle().getCurrentOdometer();
        if (dto.getStartingMileage() < vehicleCurrentOdometer) {
            log.error("Validation Error: Entered mileage {} is less than vehicle current odometer {}.",
                    dto.getStartingMileage(), vehicleCurrentOdometer);
            throw new IllegalArgumentException("Starting mileage (" + dto.getStartingMileage()
                    + " km) cannot be less than vehicle's current odometer (" + vehicleCurrentOdometer + " km).");
        }

        record.setStartingMileage(dto.getStartingMileage());
        if (record.getStatus() == HandoverStatus.PENDING) {
            record.setStatus(HandoverStatus.IN_PROGRESS);
        }

        HandoverRecord updated = handoverRepository.save(record);
        log.info("Starting mileage recorded as {} for Handover ID: {}", dto.getStartingMileage(), id);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public HandoverResponseDTO recordDamage(Long id, DamageInspectionDTO dto) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED || record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record damage inspection for a handover that is already " + record.getStatus() + ".");
        }

        if (dto.getHasDamage() == null) {
            throw new IllegalArgumentException("Please specify whether vehicle damage was found or confirmed clean.");
        }

        if (Boolean.TRUE.equals(dto.getHasDamage())) {
            if (dto.getDamageDescription() == null || dto.getDamageDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Damage description is required when damage is reported.");
            }
            record.setHasDamage(true);
            record.setDamageDescription(dto.getDamageDescription().trim());
            log.info("Damage recorded for Handover ID: {}: {}", id, dto.getDamageDescription());
        } else {
            record.setHasDamage(false);
            record.setDamageDescription(dto.getDamageDescription() != null && !dto.getDamageDescription().trim().isEmpty()
                    ? dto.getDamageDescription().trim()
                    : "Confirmed no new damage");
            log.info("Confirmed no damage for Handover ID: {}", id);
        }

        if (record.getStatus() == HandoverStatus.PENDING) {
            record.setStatus(HandoverStatus.IN_PROGRESS);
        }

        HandoverRecord updated = handoverRepository.save(record);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public HandoverResponseDTO completeHandover(Long id) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED) {
            throw new IllegalStateException("Handover record #" + id + " is already completed.");
        }
        if (record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled handover record #" + id + ".");
        }

        // Required workflow validation 1: Starting mileage must be entered
        if (record.getStartingMileage() == null) {
            throw new IllegalStateException("Cannot complete handover: Starting mileage must be recorded before completion.");
        }

        // Required workflow validation 3: Damage inspection must be performed
        if (record.getDamageDescription() == null) {
            throw new IllegalStateException("Cannot complete handover: Vehicle damage inspection must be completed before completion.");
        }

        // A security deposit must be authorized before keys are released.
        if (record.getBookingId() != null && !paymentService.hasActiveSecurityHoldForBooking(record.getBookingId())) {
            throw new IllegalStateException("Cannot complete handover: a successful security deposit hold is required before renting the vehicle.");
        }

        // Update Vehicle: acquire lock, update odometer and change status to RENTED
        Vehicle vehicle = vehicleRepository.findByIdWithLock(record.getVehicle().getId())
                .orElse(record.getVehicle());
        vehicle.setCurrentOdometer(record.getStartingMileage());
        try {
            vehicle.rent();
        } catch (Throwable t) {
            log.warn("Vehicle state pattern transition warning: {}", t.getMessage());
            vehicle.setStatus(VehicleStatus.RENTED);
        }
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} status set to RENTED, odometer updated to {}",
                vehicle.getLicensePlate(), record.getStartingMileage());

        // Update associated Booking: validate double-booking and transition from PENDING -> CONFIRMED -> ACTIVE
        if (record.getBookingId() != null) {
            Booking booking = bookingRepository.findById(record.getBookingId())
                    .orElseThrow(() -> new IllegalStateException("Associated booking #" + record.getBookingId() + " not found."));

            // Concurrency guard: ensure no other active reservation overlaps with this vehicle and time slot
            boolean hasOverlap = bookingRepository.hasOverlappingActiveBookings(
                    vehicle.getId(),
                    booking.getBookingId(),
                    booking.getPickupDateTime(),
                    booking.getReturnDateTime()
            );

            if (hasOverlap) {
                log.error("Double booking detected upon handover completion for vehicle {}", vehicle.getLicensePlate());
                throw new IllegalStateException("Cannot complete handover: Another active or confirmed booking overlaps with this vehicle and time window.");
            }

            // After pre-handover inspection finishes, transition booking: PENDING -> CONFIRMED -> ACTIVE
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.makeConfirmed();
            }
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.makeActive();
            }
            bookingRepository.save(booking);
            log.info("Booking #{} confirmed and activated upon successful handover inspection completion.", booking.getBookingId());
        }

        // Update HandoverRecord: mark COMPLETED
        record.setStatus(HandoverStatus.COMPLETED);
        record.setPaymentVerified(record.getBookingId() == null || paymentService.hasActiveSecurityHoldForBooking(record.getBookingId()));
        record.setCompletionDateTime(LocalDateTime.now());
        HandoverRecord savedRecord = handoverRepository.save(record);
        log.info("Handover ID: {} marked as COMPLETED successfully.", id);

        return mapToResponseDTO(savedRecord);
    }

    @Override
    @Transactional
    public HandoverResponseDTO cancelHandover(Long id) {
        HandoverRecord record = handoverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Handover record not found with ID: #" + id));

        if (record.getStatus() == HandoverStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed handover record #" + id + ".");
        }
        if (record.getStatus() == HandoverStatus.CANCELLED) {
            throw new IllegalStateException("Handover record #" + id + " is already cancelled.");
        }

        // Revert Vehicle status to AVAILABLE if currently RESERVED or RENTED
        try {
            Vehicle vehicle = vehicleRepository.findById(record.getVehicle().getId()).orElse(record.getVehicle());
            if (vehicle.getStatus() == VehicleStatus.RESERVED || vehicle.getStatus() == VehicleStatus.RENTED) {
                try {
                    vehicle.makeAvailable();
                } catch (Throwable t) {
                    vehicle.setStatus(VehicleStatus.AVAILABLE);
                }
                vehicleRepository.save(vehicle);
                log.info("Vehicle {} reverted to AVAILABLE upon handover cancellation.", vehicle.getLicensePlate());
            }
        } catch (Throwable t) {
            log.error("Failed to revert vehicle state on handover cancel: {}", t.getMessage());
        }

        // Sync associated Booking to CANCELLED
        if (record.getBookingId() != null) {
            try {
                bookingRepository.findById(record.getBookingId()).ifPresent(booking -> {
                    if (booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.COMPLETED) {
                        booking.makeCancelled();
                        bookingRepository.save(booking);
                        log.info("Booking #{} marked CANCELLED upon handover cancellation.", booking.getBookingId());
                    }
                });
            } catch (Exception e) {
                log.error("Failed to sync booking cancellation for booking #{}: {}", record.getBookingId(), e.getMessage());
            }
        }

        record.setStatus(HandoverStatus.CANCELLED);
        HandoverRecord updated = handoverRepository.save(record);
        log.info("Handover ID: {} cancelled successfully.", id);
        return mapToResponseDTO(updated);
    }

    private HandoverResponseDTO mapToResponseDTO(HandoverRecord record) {
        Vehicle vehicle = record.getVehicle();
        return HandoverResponseDTO.builder()
                .id(record.getId())
                .vehicleId(vehicle != null ? vehicle.getId() : null)
                .vehicleLicensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                .vehicleModel(vehicle != null ? vehicle.getModel() : null)
                .vehicleYear(vehicle != null ? vehicle.getYear() : 0)
                .vehicleStatus(vehicle != null && vehicle.getStatus() != null ? vehicle.getStatus().name() : null)
                .vehicleCurrentOdometer(vehicle != null ? vehicle.getCurrentOdometer() : 0.0)
                .bookingId(record.getBookingId())
                .customerId(record.getCustomerId())
                .customerName(record.getCustomerName())
                .staffId(record.getStaffId())
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .startingMileage(record.getStartingMileage())
                .hasDamage(record.isHasDamage())
                .paymentVerified(record.isPaymentVerified())
                .damageDescription(record.getDamageDescription())
                .fuelLevel(record.getFuelLevel())
                .notes(record.getNotes())
                .handoverDateTime(record.getHandoverDateTime())
                .completionDateTime(record.getCompletionDateTime())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
