package com.smartmoneymanager.backend.exception;

/** Generic "the request is well-formed but violates a business rule" exception (HTTP 400). */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
