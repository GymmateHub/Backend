package com.gymmate.access.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking physical access to a gym facility.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "access_logs", indexes = {
    @Index(name = "idx_access_member", columnList = "member_id, access_time DESC")
})
public class AccessLog extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "access_time", nullable = false)
    private LocalDateTime accessTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private AccessDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccessStatus status;

    @Column(name = "access_method", nullable = false, length = 50)
    private String accessMethod;

    @Column(name = "denial_reason")
    private String denialReason;

    public enum AccessDirection {
        ENTRY, EXIT
    }

    public enum AccessStatus {
        GRANTED, DENIED_PASSBACK, DENIED_MEMBERSHIP, DENIED_LOCKOUT, ALERT_TAILGATING
    }
}
