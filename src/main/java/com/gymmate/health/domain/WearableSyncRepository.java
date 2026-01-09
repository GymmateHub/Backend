package com.gymmate.health.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for WearableSync.
 * Defines domain-level operations for managing wearable device synchronization.
 */
public interface WearableSyncRepository {

    /**
     * Save or update a wearable sync record.
     */
    WearableSync save(WearableSync wearableSync);

    /**
     * Find wearable sync by ID.
     */
    Optional<WearableSync> findById(UUID id);

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
}
