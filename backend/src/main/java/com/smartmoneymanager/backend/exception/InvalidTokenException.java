package com.smartmoneymanager.backend.exception;

/** Thrown for a token (email verification / password reset / JWT refresh) that is missing, malformed, expired, or already used. */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
