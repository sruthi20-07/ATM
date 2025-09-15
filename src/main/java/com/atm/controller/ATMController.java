package com.atm.controller;

import com.atm.model.User;
import com.atm.service.AtmService;
import com.atm.service.EmailService;
import com.atm.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/atm") // All routes under /atm
public class ATMController {

    @Autowired
    private AtmService atmService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // Root → send to login or dashboard
    @GetMapping("")
    public String entry(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user == null) ? "redirect:/atm/login" : "redirect:/atm/dashboard";
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        model.addAttribute("user", user);
        model.addAttribute("balance", user.getBalance());
        model.addAttribute("transactions", atmService.getMiniStatement(user));
        return "dashboard";
    }

    // =================== SHOW PAGES ===================
    @GetMapping("/deposit")
    public String showDepositPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        model.addAttribute("user", user);
        return "deposit";
    }

    @GetMapping("/withdraw")
    public String showWithdrawPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        model.addAttribute("user", user);
        return "withdraw";
    }

    @GetMapping("/balance")
    public String showBalancePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        model.addAttribute("balance", user.getBalance());
        return "balance";
    }

    @GetMapping("/mini-statement")
    public String showMiniStatementPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        model.addAttribute("transactions", atmService.getMiniStatement(user));
        return "mini-statement";
    }

    // =================== TRANSACTIONS ===================

    // Deposit
    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount, HttpSession session, RedirectAttributes ra, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";

        try {
            String result = atmService.deposit(user, amount);
            if ("OTP_REQUIRED".equals(result)) {
                String otp = otpService.generateOtp(user.getEmail());
                emailService.sendEmail(user.getEmail(), "Deposit OTP", "Your OTP is: " + otp);
                ra.addFlashAttribute("amount", amount);
                ra.addFlashAttribute("info", "We’ve sent an OTP to your registered email (" + maskEmail(user.getEmail()) + ").");
                return "redirect:/atm/verify-deposit-otp";
            } else {
                setReceiptModel(model, user, "DEPOSIT", amount);
                model.addAttribute("status", "SUCCESS");
                model.addAttribute("message", "Deposit successful");
                return "transaction-result";
            }
        } catch (Exception e) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "❌ Transaction Failed: " + e.getMessage());
            return "transaction-result";
        }
    }

    // Withdraw
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount, HttpSession session, RedirectAttributes ra, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";

        try {
            String result = atmService.withdraw(user, amount);
            if ("OTP_REQUIRED".equals(result)) {
                String otp = otpService.generateOtp(user.getEmail());
                emailService.sendEmail(user.getEmail(), "Withdraw OTP", "Your OTP is: " + otp);
                ra.addFlashAttribute("amount", amount);
                ra.addFlashAttribute("info", "We’ve sent an OTP to your registered email (" + maskEmail(user.getEmail()) + ").");
                return "redirect:/atm/verify-withdraw-otp";
            } else {
                setReceiptModel(model, user, "WITHDRAW", amount);
                model.addAttribute("status", "SUCCESS");
                model.addAttribute("message", "Withdrawal successful");
                return "transaction-result";
            }
        } catch (Exception e) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "❌ Transaction Failed: " + e.getMessage());
            return "transaction-result";
        }
    }

    // =================== OTP PAGES (GET) ===================
    // These guarantee there's a page to enter the OTP after redirect

    @GetMapping("/verify-deposit-otp")
    public String showVerifyDepositOtp(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        // amount + info come via FlashAttributes; if missing, show a gentle hint
        if (!model.containsAttribute("amount")) model.addAttribute("amount", 0);
        if (!model.containsAttribute("info")) model.addAttribute("info", "Enter the OTP sent to your email.");
        return "verify-deposit-otp";
    }

    @GetMapping("/verify-withdraw-otp")
    public String showVerifyWithdrawOtp(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";
        if (!model.containsAttribute("amount")) model.addAttribute("amount", 0);
        if (!model.containsAttribute("info")) model.addAttribute("info", "Enter the OTP sent to your email.");
        return "verify-withdraw-otp";
    }

    // =================== OTP VERIFY (POST) ===================

    @PostMapping("/verify-withdraw-otp")
    public String verifyWithdrawOtp(@RequestParam String otp,
                                    @RequestParam double amount,
                                    HttpSession session,
                                    Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";

        try {
            boolean isValid = otpService.validateOtp(user.getEmail(), otp);
            if (isValid) {
                atmService.confirmWithdraw(user, amount);
                setReceiptModel(model, user, "WITHDRAW", amount);
                model.addAttribute("status", "SUCCESS");
                model.addAttribute("message", "Withdrawal successful");
                return "transaction-result";
            } else {
                model.addAttribute("errorMessage", "❌ Invalid OTP. Try again.");
                model.addAttribute("amount", amount);
                return "verify-withdraw-otp";
            }
        } catch (Exception e) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "❌ Transaction Failed: " + e.getMessage());
            return "transaction-result";
        }
    }

    @PostMapping("/verify-deposit-otp")
    public String verifyDepositOtp(@RequestParam String otp,
                                   @RequestParam double amount,
                                   HttpSession session,
                                   Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/atm/login";

        try {
            boolean isValid = otpService.validateOtp(user.getEmail(), otp);
            if (isValid) {
                atmService.confirmDeposit(user, amount);
                setReceiptModel(model, user, "DEPOSIT", amount);
                model.addAttribute("status", "SUCCESS");
                model.addAttribute("message", "Deposit successful");
                return "transaction-result";
            } else {
                model.addAttribute("errorMessage", "❌ Invalid OTP. Try again.");
                model.addAttribute("amount", amount);
                return "verify-deposit-otp";
            }
        } catch (Exception e) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "❌ Transaction Failed: " + e.getMessage());
            return "transaction-result";
        }
    }

    // =================== HELPER ===================
    private void setReceiptModel(Model model, User user, String txnType, double amount) {
        model.addAttribute("user", user);
        model.addAttribute("txnType", txnType);
        model.addAttribute("amount", amount);
        model.addAttribute("balance", user.getBalance());
        model.addAttribute("timestamp", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        model.addAttribute("accountNumber", user.getAccountNumber());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "your email";
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        String masked = name.length() <= 2 ? name.charAt(0) + "*" : name.substring(0, 2) + "***";
        return masked + "@" + domain;
    }
}
