package com.gymmate.identity.internal.application.dto;
public record RegistrationResponse(
    String userId,
    String message,
    int expiresIn, // seconds
    Long retryAfter // seconds (for rate limiting)
) {
}
