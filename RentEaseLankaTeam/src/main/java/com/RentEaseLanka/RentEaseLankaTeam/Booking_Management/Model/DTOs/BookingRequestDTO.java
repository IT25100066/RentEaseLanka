<<<<<<< HEAD
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingRequestDTO {
    private Long vehicleId;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalAmount;
    private boolean withDriver;
}
=======
package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingRequestDTO {
    private Long vehicleId;
    private LocalDateTime pickupDateTime;
    private LocalDateTime returnDateTime;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalAmount;
    private boolean withDriver;
}
>>>>>>> 21bee750d2eb336b0bafde9dc94de064033fd540
