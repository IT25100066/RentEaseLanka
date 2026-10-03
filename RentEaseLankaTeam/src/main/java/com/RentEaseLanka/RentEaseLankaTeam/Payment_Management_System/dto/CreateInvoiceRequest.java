package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateInvoiceRequest {
    @NotNull(message = "Vehicle reservation ID is required")
    public Long vehicleReservationId;

    @NotNull(message = "Customer ID is required")
    public Long customerId;

    @NotNull @Positive(message = "Rental days must be at least 1")
    public Integer rentalDays;

    @NotNull @Positive(message = "Daily rate must be positive")
    public BigDecimal dailyRate;

    @NotNull @Positive(message = "Subtotal must be positive")
    public BigDecimal subtotal;

    @NotNull @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    public BigDecimal taxAmount;

    @NotNull(message = "Pickup deadline timestamp is required")
    public LocalDateTime pickupDeadline;
}
