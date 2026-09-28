package com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Model.DTO.FinancialSummaryDTO;

import java.time.LocalDate;

public interface FinancialService {
    FinancialSummaryDTO getIncomeSummary(LocalDate from, LocalDate to, BookingStatus status);
}
