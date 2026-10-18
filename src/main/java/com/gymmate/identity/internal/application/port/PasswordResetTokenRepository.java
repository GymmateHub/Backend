package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.identity.internal.domain.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for password reset tokens.
 */
public interface PasswordResetTokenRepository extends DomainRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser_Id(UUID userId);
}
