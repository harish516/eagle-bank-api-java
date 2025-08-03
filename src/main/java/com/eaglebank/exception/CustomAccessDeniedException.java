package com.eaglebank.exception;

/**
 * Exception thrown when access is denied due to insufficient permissions or authentication issues.
 * This exception should result in a 403 Forbidden HTTP response.
 */
public class CustomAccessDeniedException extends RuntimeException {
    
    public CustomAccessDeniedException(String message) {
        super(message);
    }
    
    public CustomAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
