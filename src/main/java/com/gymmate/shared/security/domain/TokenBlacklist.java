package com.gymmate.shared.security.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Entity to track blacklisted JWT tokens.
 * Tokens are added to blacklist when users logout or when tokens need to be
 * revoked.
 */
@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "blacklisted_at", nullable = false)
    @Builder.Default
    private LocalDateTime blacklistedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(name = "reason", length = 255)
    private String reason;

    public static TokenBlacklist create(String token, UUID userId, Date expiresAt) {
        return TokenBlacklist.builder()
                .token(token)
                .userId(userId)
                .expiresAt(expiresAt)
                .blacklistedAt(LocalDateTime.now())
                .reason("User logout")
                .build();
    }

    public static TokenBlacklist create(String token, UUID userId, Date expiresAt, String reason) {
        return TokenBlacklist.builder()
                .token(token)
                .userId(userId)
                .expiresAt(expiresAt)
                .blacklistedAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }

    public boolean isExpired() {
        return expiresAt.before(new Date());
    }
}
