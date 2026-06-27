package com.gymmate.access.api.dto;

import com.gymmate.access.domain.AccessCredential;
import com.gymmate.access.domain.enums.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessCredentialResponse(
    UUID id,
    UUID memberId,
    CredentialType type,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt,
    boolean active
) {
  public static AccessCredentialResponse fromEntity(AccessCredential c) {
    return new AccessCredentialResponse(
        c.getId(), c.getMemberId(), c.getType(), c.getIssuedAt(), c.getExpiresAt(), c.isActive());
  }
}
