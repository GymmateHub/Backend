package com.gymmate.access.domain;

import com.gymmate.access.domain.enums.CredentialType;
import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A member's access credential. The raw token is shown once at issue time;
 * only its hash is stored. Revocation uses the inherited {@code active} flag.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "access_credentials")
public class AccessCredential extends GymScopedEntity {

  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  @Builder.Default
  private CredentialType type = CredentialType.QR;

  /** SHA-256 hex of the raw credential token. Unique per gym. */
  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Column(name = "issued_at")
  @Builder.Default
  private LocalDateTime issuedAt = LocalDateTime.now();

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }
}
