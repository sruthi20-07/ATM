package com.atm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    /**
     * Send transaction alert to user (console + email).
     * @param txnType      "DEPOSIT" or "WITHDRAW"
     * @param amount       transaction amount
     * @param txnId        short transaction id/reference
     * @param dateTime     formatted date-time string
     * @param balanceAfter balance after the transaction
     * @param toEmail      recipient email
     */
    public void sendTransactionAlert(String txnType,
                                     double amount,
                                     String txnId,
                                     String dateTime,
                                     double balanceAfter,
                                     String toEmail) {

        // choose wording based on type
        final boolean isDeposit = "DEPOSIT".equalsIgnoreCase(txnType);
        final String actionWord = isDeposit ? "credited" : "debited";
        final String preposition = isDeposit ? "to" : "from";

        String message = String.format(
            "Dear SBI Customer, Rs.%.2f %s %s your account on %s.%n" +
            "Transaction Number: %s.%n" +
            "Available Balance: Rs.%.2f.%n" +
            "If not done by you, contact bank immediately.",
            amount,
            actionWord,
            preposition,
            dateTime,
            txnId,
            balanceAfter
        );

        // Console log (like SMS)
        System.out.println("📩 Transaction Alert: " + message);

        // Send email (failures are logged but do not break the flow)
        try {
            emailService.sendEmail(toEmail, "SBI ATM Transaction Alert", message);
        } catch (Exception e) {
            System.out.println("❌ Failed to send transaction alert email: " + e.getMessage());
        }
    }

    /**
     * Legacy method kept for backward compatibility.
     * Prefer {@link #sendTransactionAlert(String, double, String, String, double, String)}.
     * This assumes a withdrawal (debited) wording.
     */
    @Deprecated
    public void sendWithdrawalAlert(double amount,
                                    String txnId,
                                    String dateTime,
                                    double balanceAfter,
                                    String toEmail) {
        // fallback: treat as WITHDRAW to keep old callers compiling
        sendTransactionAlert("WITHDRAW", amount, txnId, dateTime, balanceAfter, toEmail);
    }
}
