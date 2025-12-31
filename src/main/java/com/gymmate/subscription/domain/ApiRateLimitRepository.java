package com.gymmate.subscription.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiRateLimitRepository extends JpaRepository<ApiRateLimit, UUID> {

    Optional<ApiRateLimit> findByOrganisationIdAndWindowStartAndWindowType(
        UUID organisationId,
        LocalDateTime windowStart,
        String windowType
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " +
           "AND arl.windowStart <= :now AND arl.windowEnd > :now " +
           "AND arl.windowType = :windowType")
    Optional<ApiRateLimit> findCurrentWindow(
        @Param("organisationId") UUID organisationId,
        @Param("now") LocalDateTime now,
        @Param("windowType") String windowType
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " +
           "AND arl.isBlocked = true AND arl.blockedUntil > :now")
    List<ApiRateLimit> findActiveBlocks(
        @Param("organisationId") UUID organisationId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.windowEnd < :cutoffDate")
    List<ApiRateLimit> findExpiredWindows(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteByWindowEndBefore(LocalDateTime cutoffDate);

    @Query("SELECT COUNT(arl) FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " +
           "AND arl.isBlocked = true AND arl.blockedUntil >= :since")
    Long countBlocksSince(@Param("organisationId") UUID organisationId, @Param("since") LocalDateTime since);
}

