package com.gymmate.identity.internal.infrastructure.persistence;

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
public interface TokenBlacklistJpaRepository extends JpaRepository<TokenBlacklistJpaEntity, UUID> {

    boolean existsByToken(String token);

    Optional<TokenBlacklistJpaEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(Date now);

    @Query("SELECT COUNT(t) FROM TokenBlacklist t WHERE t.expiresAt < :now")
    long countExpiredTokens(Date now);
}
