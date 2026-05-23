package com.atm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private String accountNumber;
    private String pin;
    private double balance;
    private String bankName;
    private String email;

    public Account() {}

    public Account(String accountNumber, String pin, double balance, String bankName, String email) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.bankName = bankName;
        this.email = email;
    }

    // getters and setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
