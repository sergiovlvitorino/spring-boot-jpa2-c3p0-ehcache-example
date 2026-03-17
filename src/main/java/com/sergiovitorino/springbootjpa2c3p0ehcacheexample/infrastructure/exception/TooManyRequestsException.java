package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
