package com.atm.service;

import com.atm.exception.InsufficientBalance;
import com.atm.exception.InvalidAmount;
import com.atm.model.TransactionLog;
import com.atm.model.User;
import com.atm.repository.TransactionLogRepository;
import com.atm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AtmService {

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService; // sends email/console alerts

    // Withdraw with OTP if > 10000
    public String withdraw(User user, double amount) {
        if (amount <= 0) throw new InvalidAmount("❌ Invalid amount!");
        if (amount > user.getBalance()) throw new InsufficientBalance("❌ Insufficient balance.");

        if (amount > 10000) {
            return "OTP_REQUIRED"; // Controller handles OTP
        }

        return completeTransaction(user, "WITHDRAW", amount);
    }

    // Deposit with OTP if > 10000
    public String deposit(User user, double amount) {
        if (amount <= 0) throw new InvalidAmount("❌ Invalid deposit amount!");

        if (amount > 10000) {
            return "OTP_REQUIRED";
        }

        return completeTransaction(user, "DEPOSIT", amount);
    }

    // Confirm withdraw after OTP
    public String confirmWithdraw(User user, double amount) {
        if (amount <= 0) return "❌ Invalid amount!";
        if (amount > user.getBalance()) return "❌ Insufficient balance.";

        return completeTransaction(user, "WITHDRAW", amount);
    }

    // Confirm deposit after OTP
    public String confirmDeposit(User user, double amount) {
        if (amount <= 0) return "❌ Invalid deposit amount!";

        return completeTransaction(user, "DEPOSIT", amount);
    }

    // Mini statement (last 5 transactions per user)
    public List<TransactionLog> getMiniStatement(User user) {
        return transactionLogRepository.findTop5ByUserOrderByTimestampDesc(user);
    }

    // =================== CORE TRANSACTION LOGIC ===================
    private String completeTransaction(User user, String type, double amount) {
        // Update balance
        if ("WITHDRAW".equals(type)) {
            user.setBalance(user.getBalance() - amount);
        } else {
            user.setBalance(user.getBalance() + amount);
        }

        // Persist updated balance
        userRepository.save(user);

        // Save transaction log
        TransactionLog log = new TransactionLog();
        log.setUser(user);
        log.setAmount(amount);
        log.setBalanceAfter(user.getBalance());
        log.setType(type);
        log.setTimestamp(LocalDateTime.now());
        transactionLogRepository.save(log);

        // Send alert with correct wording (credited for DEPOSIT, debited for WITHDRAW)
        String txnId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        notificationService.sendTransactionAlert(
                type,           // "DEPOSIT" or "WITHDRAW"
                amount,
                txnId,
                dateTime,
                user.getBalance(),
                user.getEmail()
        );

        // Build receipt text (optional; you’re showing a Thymeleaf receipt page)
        return formatReceipt(user, type, amount, txnId);
    }

    private String formatReceipt(User user, String type, double amount, String txnId) {
        return "✅ Transaction Successful!\n" +
                "Name: " + user.getName() + "\n" +
                "Type: " + type + "\n" +
                "Amount: ₹" + amount + "\n" +
                "Balance: ₹" + user.getBalance() + "\n" +
                "Transaction ID: " + txnId + "\n" +
                "Date & Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n" +
                "Account Number: " + user.getAccountNumber();
    }
}
