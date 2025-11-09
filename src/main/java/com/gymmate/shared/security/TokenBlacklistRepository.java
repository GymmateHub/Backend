package com.gymmate.shared.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing blacklisted tokens.
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {

    /**
     * Check if a token is blacklisted
     */
    boolean existsByToken(String token);

    /**
     * Find a blacklisted token entry by token string
     */
    Optional<TokenBlacklist> findByToken(String token);

    /**
     * Delete all expired tokens from the blacklist.
     * This should be run periodically as a cleanup job.
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(Date now);

    /**
     * Count expired tokens
     */
    @Query("SELECT COUNT(t) FROM TokenBlacklist t WHERE t.expiresAt < :now")
    long countExpiredTokens(Date now);
}


