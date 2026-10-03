package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private Long paymentMethodId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentType paymentType = PaymentType.RENTAL_FEE;

    @Column(unique = true, length = 64)
    private String txnReference;

    @Column
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}