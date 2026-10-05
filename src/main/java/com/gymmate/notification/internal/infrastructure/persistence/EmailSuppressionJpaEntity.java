package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.gymmate.notification.internal.domain.EmailSuppression;
import com.gymmate.notification.internal.domain.SuppressionReason;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link EmailSuppression} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "EmailSuppression")
@Table(name = "email_suppressions")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(EmailSuppression.class)
public class EmailSuppressionJpaEntity extends BaseAuditJpaEntity {

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
    private int transientBounceCount = 1;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "gym_id")
    private UUID gymId;
}
