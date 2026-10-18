package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.enums.AccessPointMode;
import com.gymmate.access.internal.domain.enums.AccessPointType;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.gymmate.access.internal.domain.AccessPoint;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AccessPoint} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AccessPoint")
@Table(name = "access_points")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AccessPoint.class)
public class AccessPointJpaEntity extends GymScopedJpaEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessPointType type = AccessPointType.MAIN_DOOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessPointMode mode = AccessPointMode.SOFTWARE;

    /**
     * Optional link to a {@code GymArea} this point guards.
     */
    @Column(name = "area_id")
    private UUID areaId;

    /**
     * Hardware device identifier (for TURNSTILE/CV modes).
     */
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "online")
    private boolean online = true;

    /**
     * Cooldown before the same credential may grant entry again (pass-back defence).
     */
    @Column(name = "reentry_lockout_seconds")
    private Integer reentryLockoutSeconds = 300;
}
