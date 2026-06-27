package com.gymmate.access.api.dto;

import com.gymmate.access.application.IssuedCredential;
import com.gymmate.access.domain.enums.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Returned once on credential issue — includes the raw token (shown only here).
 */
public record CredentialIssuedResponse(
    UUID id,
    UUID memberId,
    CredentialType type,
    String token,
    LocalDateTime expiresAt
) {
  public static CredentialIssuedResponse from(IssuedCredential issued) {
    return new CredentialIssuedResponse(
        issued.credential().getId(),
        issued.credential().getMemberId(),
        issued.credential().getType(),
        issued.rawToken(),
        issued.credential().getExpiresAt());
  }
}
