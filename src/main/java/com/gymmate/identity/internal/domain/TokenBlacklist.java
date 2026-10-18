package com.gymmate.identity.internal.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Entity to track blacklisted JWT tokens.
 * Tokens are added to blacklist when users logout or when tokens need to be
 * revoked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    private UUID id;

    private String token;

    private UUID userId;

    @Builder.Default
    private LocalDateTime blacklistedAt = LocalDateTime.now();

    private Date expiresAt;

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
