package com.gymmate.user.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Check-in credential for the member mobile app.
 * The app renders this payload as a QR code that front-desk staff scan
 * to look the member up by membership number.
 */
public record MemberQrCodeResponse(
    UUID memberId,
    UUID gymId,
    String membershipNumber,
    String fullName,
    String status,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt) {
}
