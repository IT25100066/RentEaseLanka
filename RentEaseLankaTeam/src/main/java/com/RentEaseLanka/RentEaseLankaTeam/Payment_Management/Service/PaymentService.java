package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Service;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.DamageDeductionRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentRequestDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Model.DTO.PaymentResponseDTO;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

import java.math.BigDecimal;

public interface PaymentService {
    BigDecimal getDepositAmount();
    PaymentResponseDTO payDeposit(Long bookingId, User customer, PaymentRequestDTO request);
    PaymentResponseDTO payFinalAmount(Long bookingId, User customer, PaymentRequestDTO request);
    PaymentResponseDTO recordDamageDeduction(Long bookingId, DamageDeductionRequestDTO request, User staff);
    boolean hasSuccessfulDeposit(Long bookingId);
    boolean hasSuccessfulFinalPayment(Long bookingId);
    void settleBooking(Long bookingId);
}
