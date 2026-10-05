package com.gymmate.membership.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;

/**
 * MembershipPlan entity representing a membership plan offered by a gym.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MembershipPlan extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String name;

  private String description;

  private BigDecimal price;

  private String billingCycle; // monthly, quarterly, yearly, lifetime

  private Integer durationMonths; // NULL for lifetime

  // Features
  private Integer classCredits; // NULL for unlimited

  @Builder.Default
  private Integer guestPasses = 0;

  @Builder.Default
  private Integer trainerSessions = 0;

  @Builder.Default
  private String amenities = "[]"; // ["pool", "sauna", "parking"]

  // Restrictions
  @Builder.Default
  private boolean peakHoursAccess = true;

  @Builder.Default
  private boolean offPeakOnly = false;

  private String specificAreas; // ["main_gym", "pool", "studio"]

  // Status
  @Builder.Default
  private boolean featured = false;

  // Stripe integration
  private String stripeProductId;

  private String stripePriceId;

  public void updatePricing(BigDecimal price, String billingCycle) {
    this.price = price;
    this.billingCycle = billingCycle;
  }

  public void updateFeatures(Integer classCredits, Integer guestPasses, Integer trainerSessions) {
    this.classCredits = classCredits;
    this.guestPasses = guestPasses;
    this.trainerSessions = trainerSessions;
  }

  public boolean hasUnlimitedClasses() {
    return classCredits == null;
  }
}
