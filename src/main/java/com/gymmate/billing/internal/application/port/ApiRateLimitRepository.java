package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.ApiRateLimit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiRateLimitRepository extends DomainRepository<ApiRateLimit, UUID> {

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
}

