package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.ApiRateLimit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ApiRateLimitRepository {

    Optional<ApiRateLimit> findByOrganisationIdAndWindowStartAndWindowType(
        UUID organisationId,
        LocalDateTime windowStart,
        String windowType
    );

    Optional<ApiRateLimit> findCurrentWindow(
        UUID organisationId,
        LocalDateTime now,
        String windowType
    );

    List<ApiRateLimit> findActiveBlocks(
        UUID organisationId,
        LocalDateTime now
    );

    List<ApiRateLimit> findExpiredWindows(LocalDateTime cutoffDate);

    void deleteByWindowEndBefore(LocalDateTime cutoffDate);

    Long countBlocksSince(UUID organisationId, LocalDateTime since);

ApiRateLimit save(ApiRateLimit entity);

List<ApiRateLimit> saveAll(Iterable<ApiRateLimit> entities);

Optional<ApiRateLimit> findById(UUID id);

boolean existsById(UUID id);

List<ApiRateLimit> findAll();

List<ApiRateLimit> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(ApiRateLimit entity);

void deleteAll(Iterable<ApiRateLimit> entities);

ApiRateLimit saveAndFlush(ApiRateLimit entity);

void flush();

Page<ApiRateLimit> findAll(Pageable pageable);
}

