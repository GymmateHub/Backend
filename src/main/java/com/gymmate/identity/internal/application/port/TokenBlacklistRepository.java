package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.identity.internal.domain.TokenBlacklist;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing blacklisted tokens.
 */
public interface TokenBlacklistRepository extends DomainRepository<TokenBlacklist, UUID> {

    boolean existsByToken(String token);

    Optional<TokenBlacklist> findByToken(String token);

    void deleteExpiredTokens(Date now);

    long countExpiredTokens(Date now);
}
