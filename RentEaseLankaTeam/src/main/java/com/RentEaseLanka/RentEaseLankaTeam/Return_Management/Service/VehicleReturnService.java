package com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Service.BookingService;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Repository.VehicleRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.InvoiceRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service.PaymentService;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO.ReturnInspectionRequest;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.DTO.ReturnInspectionResponse;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Model.VehicleReturnInspection;
import com.RentEaseLanka.RentEaseLankaTeam.Return_Management.Repository.VehicleReturnInspectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleReturnService {
    private final BookingRepository bookingRepository;
    private final VehicleReturnInspectionRepository inspectionRepository;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final InvoiceRepository invoiceRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public ReturnInspectionResponse inspectAndCompleteReturn(Long bookingId, ReturnInspectionRequest request) {
        if (inspectionRepository.findByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException("A return inspection already exists for booking #" + bookingId + ".");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: #" + bookingId));
        if (booking.getStatus() != BookingStatus.ACTIVE && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a Active rental can be returned and inspected.");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.makeActive();
            booking = bookingRepository.save(booking);
        }
        Vehicle vehicle = booking.getVehicle();
        if (vehicle == null)
            throw new IllegalStateException("Booking has no assigned vehicle.");
        if (request.endingMileage < vehicle.getCurrentOdometer()) {
            throw new IllegalArgumentException(
                    "Ending mileage cannot be less than the current odometer (" + vehicle.getCurrentOdometer() + ").");
        }
        if (request.hasDamage && (request.damageDescription == null || request.damageDescription.trim().isEmpty())) {
            throw new IllegalArgumentException("A damage description is required when damage is found.");
        }
        if (request.hasDamage && (request.damageAmount == null || request.damageAmount.signum() <= 0)) {
            throw new IllegalArgumentException("A positive damage amount is required when damage is found.");
        }

        VehicleReturnInspection inspection = inspectionRepository.save(VehicleReturnInspection.builder()
                .bookingId(bookingId).vehicleId(vehicle.getId()).staffId(request.staffId.trim())
                .endingMileage(request.endingMileage).hasDamage(request.hasDamage)
                .damageDescription(request.hasDamage ? request.damageDescription.trim() : null)
                .notes(request.notes == null || request.notes.trim().isEmpty() ? null : request.notes.trim())
                .inspectedAt(LocalDateTime.now()).build());

        vehicle.setCurrentOdometer(inspection.getEndingMileage());
        vehicleRepository.save(vehicle);
        // One transaction: preserve the inspection, settle the deposit, then perform
        // the
        // guarded booking completion if the invoice is fully paid.
        paymentService.settleSecurityDepositForReturn(bookingId, request.hasDamage,
                request.damageAmount, inspection.getDamageDescription());

        boolean isInvoicePaid = invoiceRepository.findByVehicleReservationId(bookingId)
                .map(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .orElse(false);

        if (isInvoicePaid) {
            bookingService.makeCompleted(bookingId);
        } else {
            log.info(
                    "Vehicle return inspection recorded for booking #{}. Awaiting final rental invoice payment before booking completion.",
                    bookingId);
        }
        return map(inspection);
    }

    @Transactional(readOnly = true)
    public ReturnInspectionResponse getByBookingId(Long bookingId) {
        return inspectionRepository.findByBookingId(bookingId).map(this::map)
                .orElseThrow(
                        () -> new IllegalArgumentException("No return inspection found for booking #" + bookingId));
    }

    private ReturnInspectionResponse map(VehicleReturnInspection i) {
        return new ReturnInspectionResponse(i.getId(), i.getBookingId(), i.getVehicleId(), i.getStaffId(),
                i.getEndingMileage(), i.isHasDamage(), i.getDamageDescription(), i.getNotes(), i.getInspectedAt());
    }
}
