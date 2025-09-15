package com.atm.exception;

public class MultipleTrails extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public MultipleTrails(String message) { super(message); }
}
