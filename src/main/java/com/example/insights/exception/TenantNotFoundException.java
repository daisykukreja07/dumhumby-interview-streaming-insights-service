package com.example.insights.exception;

/**
 * Exception thrown when a tenant is not found or tenant context is not set.
 */
public class TenantNotFoundException extends RuntimeException {
    
    public TenantNotFoundException(String message) {
        super(message);
    }
}

