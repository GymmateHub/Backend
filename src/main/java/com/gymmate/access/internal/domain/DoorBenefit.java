package com.gymmate.access.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.util.UUID;

/**
 * Maps which membership plan is permitted through which access point
 * ("door benefit"). If no DoorBenefit rows exist for an access point, all
 * active members are permitted (open by default until configured).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class DoorBenefit extends GymScopedEntity {

  private UUID accessPointId;

  private UUID membershipPlanId;
}
