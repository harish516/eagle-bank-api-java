package com.eaglebank.exception;

/**
 * Exception thrown when a user is not found in the system.
 * This exception should result in a 404 Not Found HTTP response.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
