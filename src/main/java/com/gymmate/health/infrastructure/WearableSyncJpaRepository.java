package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WearableSource;
import com.gymmate.health.domain.WearableSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for WearableSync entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface WearableSyncJpaRepository extends JpaRepository<WearableSync, UUID> {

    /**
     * Find all wearable syncs for a member.
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.memberId = :memberId AND ws.active = true ORDER BY ws.lastSyncAt DESC NULLS LAST")
    List<WearableSync> findByMemberIdOrderByLastSyncDesc(@Param("memberId") UUID memberId);

    /**
     * Find wearable sync by member and source type.
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.memberId = :memberId AND ws.sourceType = :sourceType AND ws.active = true")
    Optional<WearableSync> findByMemberIdAndSourceType(
        @Param("memberId") UUID memberId,
        @Param("sourceType") WearableSource sourceType
    );

    /**
     * Find all wearable syncs by gym.
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.gymId = :gymId AND ws.active = true ORDER BY ws.lastSyncAt DESC NULLS LAST")
    List<WearableSync> findByGymIdOrderByLastSyncDesc(@Param("gymId") UUID gymId);

    /**
     * Find wearable syncs by status.
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.syncStatus = :status AND ws.active = true ORDER BY ws.lastSyncAt DESC NULLS LAST")
    List<WearableSync> findBySyncStatus(@Param("status") String status);

    /**
     * Find wearable syncs that need syncing (not synced recently or never synced).
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.active = true AND (ws.lastSyncAt IS NULL OR ws.lastSyncAt < :lastSyncBefore) ORDER BY ws.lastSyncAt ASC NULLS FIRST")
    List<WearableSync> findSyncsNeedingUpdate(@Param("lastSyncBefore") LocalDateTime lastSyncBefore);

    /**
     * Find failed syncs by gym.
     */
    @Query("SELECT ws FROM WearableSync ws WHERE ws.gymId = :gymId AND ws.syncStatus = 'FAILED' AND ws.active = true ORDER BY ws.lastSyncAt DESC NULLS LAST")
    List<WearableSync> findFailedSyncsByGymId(@Param("gymId") UUID gymId);

    /**
     * Count wearable syncs for a member.
     */
    @Query("SELECT COUNT(ws) FROM WearableSync ws WHERE ws.memberId = :memberId AND ws.active = true")
    long countByMemberId(@Param("memberId") UUID memberId);

    /**
     * Check if member has specific wearable source connected.
     */
    @Query("SELECT COUNT(ws) > 0 FROM WearableSync ws WHERE ws.memberId = :memberId AND ws.sourceType = :sourceType AND ws.active = true")
    boolean existsByMemberIdAndSourceType(
        @Param("memberId") UUID memberId,
        @Param("sourceType") WearableSource sourceType
    );
}
