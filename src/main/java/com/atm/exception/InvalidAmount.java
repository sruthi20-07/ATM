package com.atm.exception;

public class InvalidAmount extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public InvalidAmount(String message) { super(message); }
}
