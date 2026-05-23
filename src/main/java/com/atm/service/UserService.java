package com.atm.service;

import com.atm.model.User;
import com.atm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String name, String email, String phone, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("❌ Email already registered!");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setAccountNumber(generateAccountNumber());
        user.setBalance(0.0);

        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    /**
     * ✅ Fetch user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ User not found: " + email));
    }

    private String generateAccountNumber() {
        return "AC" + (10000000 + new Random().nextInt(90000000));
    }
}
