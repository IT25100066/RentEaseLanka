package com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Model.DTO;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryDTO {
    private LocalDate from;
    private LocalDate to;
    private BookingStatus status;
    private long bookingCount;
    private BigDecimal grossRevenue;
    private BigDecimal maintenanceCost;
    private BigDecimal netRevenue;
}
