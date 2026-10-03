package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 20)
    private String maskedCardNum;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal simulatedBalance;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal heldAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate expiryDate;

    public BigDecimal getAvailableBalance() {
        return simulatedBalance.subtract(heldAmount);
    }
}
