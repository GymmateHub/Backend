package com.gymmate.health.domain;

import com.gymmate.health.domain.Enums.WearableSource;
import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "wearable_syncs", indexes = {
    @Index(name = "idx_wearable_member_source", columnList = "member_id,source_type")
})
public class WearableSync extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private WearableSource sourceType;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private String syncStatus = "PENDING"; // SUCCESS, FAILED, PENDING

    @Column(name = "external_user_id", length = 255)
    private String externalUserId; // ID from external service (Apple, Google, etc.)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sync_metadata", columnDefinition = "jsonb")
    private String syncMetadata; // Additional sync information as JSON

    @Column(name = "sync_error", columnDefinition = "TEXT")
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
