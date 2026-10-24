package com.example.styleshop.exception;

public class ReturnNotAllowedException extends RuntimeException {

    public ReturnNotAllowedException(String message) {
        super(message);
    }
}