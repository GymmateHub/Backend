package com.gymmate.notification.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.util.UUID;

/**
 * Domain entity representing an email address suppressed from sending.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class EmailSuppression extends BaseAuditEntity {

    private String email;

    private 
    SuppressionReason reason;

    private String bounceType;

    private String bounceSubType;

    private String diagnosticCode;

    @Builder.Default
    private 
    int transientBounceCount = 1;

    private UUID organisationId;

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
