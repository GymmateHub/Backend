package com.gymmate.billing.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiRateLimitJpaRepository extends JpaRepository<ApiRateLimitJpaEntity, UUID> {

    Optional<ApiRateLimitJpaEntity> findByOrganisationIdAndWindowStartAndWindowType(UUID organisationId, LocalDateTime windowStart, String windowType);

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " + "AND arl.windowStart <= :now AND arl.windowEnd > :now " + "AND arl.windowType = :windowType")
    Optional<ApiRateLimitJpaEntity> findCurrentWindow(@Param("organisationId") UUID organisationId, @Param("now") LocalDateTime now, @Param("windowType") String windowType);

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " + "AND arl.isBlocked = true AND arl.blockedUntil > :now")
    List<ApiRateLimitJpaEntity> findActiveBlocks(@Param("organisationId") UUID organisationId, @Param("now") LocalDateTime now);

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.windowEnd < :cutoffDate")
    List<ApiRateLimitJpaEntity> findExpiredWindows(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteByWindowEndBefore(LocalDateTime cutoffDate);

    @Query("SELECT COUNT(arl) FROM ApiRateLimit arl WHERE arl.organisationId = :organisationId " + "AND arl.isBlocked = true AND arl.blockedUntil >= :since")
    Long countBlocksSince(@Param("organisationId") UUID organisationId, @Param("since") LocalDateTime since);
}
