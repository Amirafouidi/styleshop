package com.example.styleshop.exception;

public class ReturnPeriodExpiredException extends RuntimeException {

    public ReturnPeriodExpiredException(String message) {
        super(message);
    }
}