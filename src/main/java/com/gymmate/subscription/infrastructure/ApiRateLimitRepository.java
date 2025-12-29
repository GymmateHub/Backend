package com.gymmate.subscription.infrastructure;

import com.gymmate.subscription.domain.ApiRateLimit;
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

    Optional<ApiRateLimit> findByGymIdAndWindowStartAndWindowType(
        UUID gymId,
        LocalDateTime windowStart,
        String windowType
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.gymId = :gymId " +
           "AND arl.windowStart <= :now AND arl.windowEnd > :now " +
           "AND arl.windowType = :windowType")
    Optional<ApiRateLimit> findCurrentWindow(
        @Param("gymId") UUID gymId,
        @Param("now") LocalDateTime now,
        @Param("windowType") String windowType
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.gymId = :gymId " +
           "AND arl.isBlocked = true AND arl.blockedUntil > :now")
    List<ApiRateLimit> findActiveBlocks(
        @Param("gymId") UUID gymId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT arl FROM ApiRateLimit arl WHERE arl.windowEnd < :cutoffDate")
    List<ApiRateLimit> findExpiredWindows(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteByWindowEndBefore(LocalDateTime cutoffDate);

    @Query("SELECT COUNT(arl) FROM ApiRateLimit arl WHERE arl.gymId = :gymId " +
           "AND arl.isBlocked = true AND arl.createdAt > :since")
    Long countBlocksSince(@Param("gymId") UUID gymId, @Param("since") LocalDateTime since);
}

