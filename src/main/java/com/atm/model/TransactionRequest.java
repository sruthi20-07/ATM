package com.atm.model;

public class TransactionRequest {
    private String accountNumber;
    private double amount;
    private int otp;

    public TransactionRequest() {}

    public TransactionRequest(String accountNumber, double amount, int otp) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.otp = otp;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public int getOtp() { return otp; }
    public void setOtp(int otp) { this.otp = otp; }
}
