package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.domain.enums.WearableSource;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.health.internal.domain.WearableSync;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link WearableSync} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "WearableSync")
@Table(name = "wearable_syncs", indexes = { @Index(name = "idx_wearable_member_source", columnList = "member_id,source_type") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(WearableSync.class)
public class // Business methods
WearableSyncJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private WearableSource sourceType;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "sync_status", length = 20)
    private String syncStatus = "PENDING"; // SUCCESS, FAILED, PENDING

    @Column(name = "external_user_id", length = 255)
    private String externalUserId; // ID from external service (Apple, Google, etc.)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sync_metadata", columnDefinition = "jsonb")
    private String syncMetadata; // Additional sync information as JSON

    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;
}
