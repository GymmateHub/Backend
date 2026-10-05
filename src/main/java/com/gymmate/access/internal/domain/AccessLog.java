package com.gymmate.access.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
public class AccessLog extends GymScopedEntity {

    private UUID memberId;

    private LocalDateTime accessTime;

    private 
    AccessDirection direction;

    private 
    AccessStatus status;

    private String accessMethod;

    private String denialReason;

    public enum AccessDirection {
        ENTRY, EXIT
    }

    public enum AccessStatus {
        GRANTED, DENIED_PASSBACK, DENIED_MEMBERSHIP, DENIED_LOCKOUT, ALERT_TAILGATING
    }
}
