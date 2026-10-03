<<<<<<< HEAD
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDetailsDTO {
    @JsonProperty("bookingId")
    private Long BookingId;
    private Long vehicleId;
    private String model;
    private Long customerId;
    private String customerName;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private boolean withDriver;
    private Long driverId;
    private String driverName;

    @JsonProperty("BookingId")
    public Long getCapitalBookingId() {
        return this.BookingId;
    }
}
=======
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDetailsDTO {
    @JsonProperty("bookingId")
    private Long BookingId;
    private Long vehicleId;
    private String model;
    private Long customerId;
    private String customerName;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private boolean withDriver;
    private Long driverId;
    private String driverName;

    @JsonProperty("BookingId")
    public Long getCapitalBookingId() {
        return this.BookingId;
    }
}
>>>>>>> 21bee750d2eb336b0bafde9dc94de064033fd540
