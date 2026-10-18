package com.gymmate.health.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.health.internal.domain.enums.WearableSource;
import com.gymmate.health.internal.domain.WearableSync;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for WearableSync.
 * Defines domain-level operations for managing wearable device synchronization.
 */
public interface WearableSyncRepository extends DomainRepository<WearableSync, UUID> {

    /**
     * Find all wearable syncs for a member.
     */
    List<WearableSync> findByMemberId(UUID memberId);

    /**
     * Find wearable sync by member and source type.
     */
    Optional<WearableSync> findByMemberIdAndSourceType(UUID memberId, WearableSource sourceType);

    /**
     * Find all wearable syncs by gym.
     */
    List<WearableSync> findByGymId(UUID gymId);

    /**
     * Find wearable syncs by status.
     */
    List<WearableSync> findByStatus(String syncStatus);

    /**
     * Find wearable syncs that need syncing (not synced recently).
     */
    List<WearableSync> findSyncsNeedingUpdate(LocalDateTime lastSyncBefore);

    /**
     * Find failed syncs by gym.
     */
    List<WearableSync> findFailedSyncsByGymId(UUID gymId);

    /**
     * Count wearable syncs for a member.
     */
    long countByMemberId(UUID memberId);

    /**
     * Delete a wearable sync (soft delete).
     */
    void delete(WearableSync wearableSync);

    /**
     * Check if member has specific wearable source connected.
     */
    boolean existsByMemberIdAndSourceType(UUID memberId, WearableSource sourceType);

    List<WearableSync> findByMemberIdOrderByLastSyncDesc(UUID memberId);

    List<WearableSync> findByGymIdOrderByLastSyncDesc(UUID gymId);

    List<WearableSync> findBySyncStatus(String status);
}
