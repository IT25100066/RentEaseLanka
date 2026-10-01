package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Service;

import java.math.BigDecimal;

public interface PaymentSimulator {
    SimulatorResult collect(BigDecimal amount, String idempotencyKey, String mode);
    SimulatorResult transfer(BigDecimal amount, String idempotencyKey, String mode);

    record SimulatorResult(boolean successful, String reference, String failureReason) {}
}
