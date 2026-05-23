package com.atm.controller;

import com.atm.model.User;
import com.atm.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/atm")
public class UserController {

    @Autowired
    private UserService userService;

    // Register page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // Handle registration
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String password,
                           Model model) {
        try {
            userService.registerUser(name, email, phone, password);
            model.addAttribute("message", "✅ Registration successful! Please login.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Handle login
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> userOpt = userService.loginUser(email, password);

        if (userOpt.isPresent()) {
            session.setAttribute("user", userOpt.get());
            return "redirect:/atm/dashboard";
        } else {
            model.addAttribute("error", "❌ Invalid credentials!");
            return "login";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/atm/login";
    }
}
