package com.atm.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users") // ✅ Avoids conflict with reserved word 'user' in some DBs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password;

    private String accountNumber;

    private double balance = 0.0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionLog> transactions;

    // ✅ Default Constructor (required by JPA)
    public User() {}

    // ✅ Parameterized Constructor
    public User(String name, String email, String phone, String password, String accountNumber, double balance) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    // ✅ Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<TransactionLog> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionLog> transactions) {
        this.transactions = transactions;
    }
}
