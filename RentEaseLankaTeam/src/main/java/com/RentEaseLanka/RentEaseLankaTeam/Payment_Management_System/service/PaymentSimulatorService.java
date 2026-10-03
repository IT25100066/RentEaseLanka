package com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.service;

import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.PaymentMethod;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.entity.SimulatorConfig;
import com.RentEaseLanka.RentEaseLankaTeam.Payment_Management_System.repository.SimulatorConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentSimulatorService {

    private final SimulatorConfigRepository configRepository;

    public SimulationResponse simulateTransaction(PaymentMethod method, BigDecimal amount) {
        // 1. Trigger Check
        Optional<SimulatorConfig> ruleOpt = configRepository.findByTriggerPattern(method.getMaskedCardNum());
        if (ruleOpt.isPresent()) {
            SimulatorConfig rule = ruleOpt.get();
            injectLatency(rule.getLatencyMs());
            if (!"200_APPROVED".equals(rule.getResponseCode())) {
                return new SimulationResponse(false, rule.getResponseCode(), null);
            }
        }

        // 2. Expiry Check
        if (method.getExpiryDate().isBefore(LocalDate.now())) {
            return new SimulationResponse(false, "400_CARD_EXPIRED", null);
        }

        // 3. Balance Check
        if (method.getAvailableBalance().compareTo(amount) < 0) {
            return new SimulationResponse(false, "402_INSUFFICIENT_FUNDS", null);
        }

        // 4. Balance Deduction & Approval
        method.setSimulatedBalance(method.getSimulatedBalance().subtract(amount));
        String txnRef = "TXN-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new SimulationResponse(true, "200_APPROVED", txnRef);
    }

    public SimulationResponse simulatePreAuthHold(PaymentMethod method, BigDecimal amount) {
        if (method.getExpiryDate().isBefore(LocalDate.now())) {
            return new SimulationResponse(false, "400_CARD_EXPIRED", null);
        }

        if (method.getAvailableBalance().compareTo(amount) < 0) {
            return new SimulationResponse(false, "402_INSUFFICIENT_FUNDS", null);
        }

        // Hold funds on credit line without immediate settlement
        method.setHeldAmount(method.getHeldAmount().add(amount));
        String txnRef = "HOLD-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new SimulationResponse(true, "200_HOLD_APPROVED", txnRef);
    }

    public void releasePreAuthHold(PaymentMethod method, BigDecimal amount) {
        BigDecimal newHold = method.getHeldAmount().subtract(amount).max(BigDecimal.ZERO);
        method.setHeldAmount(newHold);
    }

    public SimulationResponse capturePreAuthHold(PaymentMethod method, BigDecimal holdAmount, BigDecimal captureAmount) {
        if (captureAmount.compareTo(holdAmount) > 0) {
            return new SimulationResponse(false, "400_CAPTURE_EXCEEDS_HOLD", null);
        }

        // Release hold and perform actual balance deduction
        releasePreAuthHold(method, holdAmount);
        method.setSimulatedBalance(method.getSimulatedBalance().subtract(captureAmount));

        String txnRef = "CAP-MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new SimulationResponse(true, "200_CAPTURE_SUCCESS", txnRef);
    }

    public void simulateRefund(PaymentMethod method, BigDecimal refundAmount) {
        method.setSimulatedBalance(method.getSimulatedBalance().add(refundAmount));
    }

    private void injectLatency(Long latencyMs) {
        if (latencyMs == null || latencyMs <= 0) return;
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record SimulationResponse(boolean success, String responseCode, String txnRef) {}
}


