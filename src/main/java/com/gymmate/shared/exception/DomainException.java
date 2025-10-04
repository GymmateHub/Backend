package com.gymmate.shared.exception;

/**
 * Exception thrown when domain business rules are violated.
 */
public class DomainException extends BaseException {
    
    public DomainException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public DomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}