package com.gymmate.notification.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Domain entity representing an email address suppressed from sending.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "email_suppressions")
public class EmailSuppression extends BaseAuditEntity {

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SuppressionReason reason;

    @Column(name = "bounce_type", length = 50)
    private String bounceType;

    @Column(name = "bounce_sub_type", length = 100)
    private String bounceSubType;

    @Column(name = "diagnostic_code", columnDefinition = "TEXT")
    private String diagnosticCode;

    @Column(name = "transient_bounce_count", nullable = false)
    @Builder.Default
    private int transientBounceCount = 1;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "gym_id")
    private UUID gymId;

    /**
     * Increment transient bounce counter.
     */
    public void incrementTransientBounce(String subType, String diagnostic) {
        this.transientBounceCount++;
        this.bounceSubType = subType;
        this.diagnosticCode = diagnostic;
    }

    /**
     * Mark as permanent bounce (e.g. after reaching transient threshold).
     */
    public void escalateToPermanent(String diagnostic) {
        this.reason = SuppressionReason.PERMANENT_BOUNCE;
        this.bounceType = "Permanent";
        this.diagnosticCode = diagnostic;
        this.setActive(true);
    }
}
