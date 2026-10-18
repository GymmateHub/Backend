package com.gymmate.identity.api.dto;

/** Freshly issued access/refresh token pair. */
public record TokenPair(String accessToken, String refreshToken) {
}
