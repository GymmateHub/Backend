package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.access.internal.domain.AccessLog;
import lombok.Getter;
import lombok.Setter;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AccessLog} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AccessLog")
@Table(name = "access_logs", indexes = { @Index(name = "idx_access_member", columnList = "member_id, access_time DESC") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AccessLog.class)
public class AccessLogJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "access_time", nullable = false)
    private LocalDateTime accessTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private AccessLog.AccessDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccessLog.AccessStatus status;

    @Column(name = "access_method", nullable = false, length = 50)
    private String accessMethod;

    @Column(name = "denial_reason")
    private String denialReason;
}
