package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Model;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTripDTO {
    private Long bookingId;
    private BookingStatus status;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalAmount;
    private Long vehicleId;
    private String vehicleModel;
    private String vehiclePlate;
    private Long customerId;
    private String customerName;
    private String customerEmail;
}
