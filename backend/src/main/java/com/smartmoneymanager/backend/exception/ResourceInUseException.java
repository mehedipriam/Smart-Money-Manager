package com.smartmoneymanager.backend.exception;

/** Thrown when deleting a resource is blocked because other records still reference it (HTTP 409). */
public class ResourceInUseException extends RuntimeException {
    public ResourceInUseException(String message) {
        super(message);
    }
}
