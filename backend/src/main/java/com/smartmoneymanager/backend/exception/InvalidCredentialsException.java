package com.smartmoneymanager.backend.exception;

/** Thrown when a user-supplied "current password" (change-password flow) does not match. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
