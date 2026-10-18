package com.gymmate.identity.internal.application.dto;
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
