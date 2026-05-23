package com.atm.exception;

public class InsufficientBalance extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InsufficientBalance(String message) { super(message); }
}
