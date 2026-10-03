package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EventType eventType;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String payloadSnapshot;

    @Column(length = 50)
    private String responseCode;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
