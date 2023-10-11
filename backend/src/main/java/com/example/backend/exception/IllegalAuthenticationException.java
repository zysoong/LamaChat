package com.example.backend.exception;

public class IllegalAuthenticationException extends RuntimeException {
    public IllegalAuthenticationException() {
        super("Authenticated user does not have access to the requested resources");
    }

    public IllegalAuthenticationException(String message) {
        super(message);
    }

    public IllegalAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
