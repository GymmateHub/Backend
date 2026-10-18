package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.enums.CredentialType;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.access.internal.domain.AccessCredential;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AccessCredential} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AccessCredential")
@Table(name = "access_credentials")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AccessCredential.class)
public class AccessCredentialJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CredentialType type = CredentialType.QR;

    /**
     * SHA-256 hex of the raw credential token. Unique per gym.
     */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
