package com.gymmate.identity.internal.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import com.gymmate.identity.internal.domain.TokenBlacklist;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link TokenBlacklist} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "TokenBlacklist")
@Table(name = "token_blacklist", indexes = { @Index(name = "idx_token", columnList = "token"), @Index(name = "idx_expires_at", columnList = "expires_at") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(TokenBlacklist.class)
public class TokenBlacklistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(name = "reason", length = 255)
    private String reason;
}
