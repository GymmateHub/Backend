package com.gymmate.identity.internal.application.port;

import com.gymmate.identity.internal.domain.TokenBlacklist;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository for managing blacklisted tokens.
 */
public interface TokenBlacklistRepository {

    boolean existsByToken(String token);

    Optional<TokenBlacklist> findByToken(String token);

    void deleteExpiredTokens(Date now);

    long countExpiredTokens(Date now);

TokenBlacklist save(TokenBlacklist entity);

List<TokenBlacklist> saveAll(Iterable<TokenBlacklist> entities);

Optional<TokenBlacklist> findById(UUID id);

boolean existsById(UUID id);

List<TokenBlacklist> findAll();

List<TokenBlacklist> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(TokenBlacklist entity);

void deleteAll(Iterable<TokenBlacklist> entities);

TokenBlacklist saveAndFlush(TokenBlacklist entity);

void flush();

Page<TokenBlacklist> findAll(Pageable pageable);
}
