package com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository.BookingRepository;
import com.RentEaseLanka.RentEaseLankaTeam.Financial_Management.Model.DTO.FinancialSummaryDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.MaintenenceManagement.Repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class FinancialServiceImpl implements FinancialService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Override
    public FinancialSummaryDTO getIncomeSummary(LocalDate from, LocalDate to, BookingStatus status) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The from date cannot be after the to date");
        }

        BookingStatus resolvedStatus = status == null ? BookingStatus.COMPLETED : status;
        LocalDateTime periodStart = from.atStartOfDay();
        LocalDateTime periodEnd = to.plusDays(1).atStartOfDay();

        long bookingCount = bookingRepository.countRevenueBookings(
            resolvedStatus, periodStart, periodEnd);
        BigDecimal grossRevenue = toBigDecimal(bookingRepository.sumRevenueBookings(
            resolvedStatus, periodStart, periodEnd));
        BigDecimal maintenanceCost = toBigDecimal(maintenanceRepository.getCompletedCost(
            periodStart, periodEnd));
        BigDecimal netRevenue = grossRevenue.subtract(maintenanceCost);

        return new FinancialSummaryDTO(
                from,
                to,
                resolvedStatus,
                bookingCount,
                grossRevenue,
                maintenanceCost,
                netRevenue);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }
}
