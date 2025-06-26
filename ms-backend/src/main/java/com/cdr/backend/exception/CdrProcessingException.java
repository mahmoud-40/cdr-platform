package com.cdr.backend.exception;

public class CdrProcessingException extends RuntimeException {
    public CdrProcessingException(String message) {
        super(message);
    }
    public CdrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
} 