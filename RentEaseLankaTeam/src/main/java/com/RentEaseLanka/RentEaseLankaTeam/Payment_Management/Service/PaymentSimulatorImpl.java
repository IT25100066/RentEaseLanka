package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management.Service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Component
public class PaymentSimulatorImpl implements PaymentSimulator {
    @Override
    public SimulatorResult collect(BigDecimal amount, String idempotencyKey, String mode) {
        return result("SIM-COLLECT", amount, idempotencyKey, mode);
    }

    @Override
    public SimulatorResult transfer(BigDecimal amount, String idempotencyKey, String mode) {
        return result("SIM-TRANSFER", amount, idempotencyKey, mode);
    }

    private SimulatorResult result(String prefix, BigDecimal amount, String idempotencyKey, String mode) {
        if (amount == null || amount.signum() < 0) {
            return new SimulatorResult(false, null, "Payment amount must not be negative.");
        }
        if ("FAIL".equalsIgnoreCase(mode)) {
            return new SimulatorResult(false, null, "Payment simulator rejected the transaction.");
        }
        String suffix = UUID.nameUUIDFromBytes((prefix + ":" + idempotencyKey).getBytes())
                .toString().substring(0, 12).toUpperCase(Locale.ROOT);
        return new SimulatorResult(true, prefix + "-" + suffix, null);
    }
}
