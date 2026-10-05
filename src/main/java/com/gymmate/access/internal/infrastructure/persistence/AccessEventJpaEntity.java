package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;
import com.gymmate.access.internal.domain.enums.DenyReason;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.access.internal.domain.AccessEvent;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AccessEvent} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AccessEvent")
@Table(name = "access_events")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AccessEvent.class)
public class AccessEventJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "access_point_id", nullable = false)
    private UUID accessPointId;

    @Column(name = "credential_id")
    private UUID credentialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccessDirection direction = AccessDirection.IN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccessDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "deny_reason", length = 30)
    private DenyReason denyReason;

    @Column(name = "tailgating_suspected")
    private boolean tailgatingSuspected = false;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    /**
     * Valid scans counted for the entry window (hardware reconciliation).
     */
    @Column(name = "valid_scan_count")
    private Integer validScanCount;

    /**
     * People detected passing through (turnstile/CV reconciliation).
     */
    @Column(name = "device_pass_count")
    private Integer devicePassCount;

    /**
     * Image captured by a CV adapter for staff review.
     */
    @Column(name = "captured_image_url", length = 500)
    private String capturedImageUrl;

    @Column(columnDefinition = "TEXT")
    private String note;
}
