package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;


import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.InvoiceStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.RentalTerm;
import jakarta.persistence.*;
import lombok.*;
import  java.time.LocalDateTime;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleReservationId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Integer rentalDays;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPartialAllowed = false;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal minimumDepositAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(nullable = false)
    private LocalDateTime pickupDeadline;

    public BigDecimal getRemainingBalance() {
        return totalAmount.subtract(paidAmount);
    }

    public RentalTerm getRentalTerm() {
        if (rentalDays <= 2) return RentalTerm.SHORT_TERM;
        if (rentalDays < 30) return RentalTerm.MEDIUM_TERM;
        return RentalTerm.LONG_TERM;
    }
}