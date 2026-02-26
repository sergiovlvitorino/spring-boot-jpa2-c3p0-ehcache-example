package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}
