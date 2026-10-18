package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.gymmate.access.internal.domain.DoorBenefit;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link DoorBenefit} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "DoorBenefit")
@Table(name = "door_benefits")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(DoorBenefit.class)
public class DoorBenefitJpaEntity extends GymScopedJpaEntity {

    @Column(name = "access_point_id", nullable = false)
    private UUID accessPointId;

    @Column(name = "membership_plan_id", nullable = false)
    private UUID membershipPlanId;
}
