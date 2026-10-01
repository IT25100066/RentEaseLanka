package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentPurpose;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.PaymentTransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponseDTO {
    private Long bookingId;
    private PaymentPurpose purpose;
    private BigDecimal amount;
    private BigDecimal remainingDeposit;
    private String currency;
    private PaymentTransactionStatus status;
    private String transactionReference;
    private String message;
}
