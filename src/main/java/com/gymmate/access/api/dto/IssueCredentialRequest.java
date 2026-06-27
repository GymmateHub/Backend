package com.gymmate.access.api.dto;

import com.gymmate.access.domain.enums.CredentialType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueCredentialRequest(
    @NotNull UUID memberId,
    CredentialType type,
    LocalDateTime expiresAt
) {
}
