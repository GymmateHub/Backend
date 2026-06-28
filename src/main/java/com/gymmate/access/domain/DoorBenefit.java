package com.gymmate.access.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
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
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "door_benefits")
public class DoorBenefit extends GymScopedEntity {

  @Column(name = "access_point_id", nullable = false)
  private UUID accessPointId;

  @Column(name = "membership_plan_id", nullable = false)
  private UUID membershipPlanId;
}
