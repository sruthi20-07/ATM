package com.atm.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private final Map<String, String> otpStore = new HashMap<>();

    // ✅ Generate OTP for a user
    public String generateOtp(String email) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        String otp = sb.toString();
        otpStore.put(email, otp);
        System.out.println("✅ OTP Generated for " + email + ": " + otp);
        return otp;
    }

    // ✅ Validate OTP
    public boolean validateOtp(String email, String userOtp) {
        String storedOtp = otpStore.get(email);
        boolean isValid = storedOtp != null && storedOtp.equals(userOtp);
        System.out.println("🔍 OTP Validation for " + email + " → " + isValid);
        if (isValid) {
            otpStore.remove(email); // OTP one-time use
        }
        return isValid;
    }
}
