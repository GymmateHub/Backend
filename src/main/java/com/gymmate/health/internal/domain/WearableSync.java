package com.gymmate.health.internal.domain;

import com.gymmate.health.internal.domain.enums.WearableSource;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WearableSync entity for tracking wearable device integration status.
 * Placeholder entity for future Apple Health, Google Fit, Fitbit integrations.
 * Implements FR-016: Wearable Integration (structure).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class WearableSync extends GymScopedEntity {

    private UUID memberId;

    private 
    WearableSource sourceType;

    private LocalDateTime lastSyncAt;

    @Builder.Default
    private 
    String syncStatus = "PENDING"; // SUCCESS, FAILED, PENDING

    private String externalUserId; // ID from external service (Apple, Google, etc.)

    private 
    String syncMetadata; // Additional sync information as JSON

    private String syncError;

    // Business methods

    public void markSuccess() {
        this.syncStatus = "SUCCESS";
        this.lastSyncAt = LocalDateTime.now();
        this.syncError = null;
    }

    public void markFailed(String error) {
        this.syncStatus = "FAILED";
        this.syncError = error;
        this.lastSyncAt = LocalDateTime.now();
    }

    public boolean isSynced() {
        return "SUCCESS".equals(syncStatus) && lastSyncAt != null;
    }

    public boolean needsSync() {
        // Sync if never synced or last sync was more than 24 hours ago
        if (lastSyncAt == null) {
            return true;
        }
        return lastSyncAt.isBefore(LocalDateTime.now().minusHours(24));
    }
}
