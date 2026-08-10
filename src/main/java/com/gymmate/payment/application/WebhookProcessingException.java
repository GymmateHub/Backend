package com.gymmate.payment.application;

/**
 * Thrown when a Stripe webhook event fails to process. Deliberately NOT a
 * {@link com.gymmate.shared.exception.DomainException} — that maps to HTTP 400 via
 * {@link com.gymmate.shared.exception.GlobalExceptionHandler}, which tells Stripe
 * "malformed, don't retry." This maps to the generic-exception handler's 500 instead,
 * so Stripe's own retry/redelivery schedule kicks in for a genuine processing failure.
 */
public class WebhookProcessingException extends RuntimeException {
    public WebhookProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
