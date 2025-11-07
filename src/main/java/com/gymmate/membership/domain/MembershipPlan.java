package com.gymmate.membership.domain;

import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "membership_plans")
public class MembershipPlan extends TenantEntity {

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "billing_cycle", nullable = false, length = 20)
  private String billingCycle; // monthly, quarterly, yearly, lifetime

  @Column(name = "duration_months")
  private Integer durationMonths; // NULL for lifetime

  // Features
  @Column(name = "class_credits")
  private Integer classCredits; // NULL for unlimited

  @Column(name = "guest_passes")
  @Builder.Default
  private Integer guestPasses = 0;

  @Column(name = "trainer_sessions")
  @Builder.Default
  private Integer trainerSessions = 0;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private String amenities = "[]"; // ["pool", "sauna", "parking"]

  // Restrictions
  @Column(name = "peak_hours_access")
  @Builder.Default
  private boolean peakHoursAccess = true;

  @Column(name = "off_peak_only")
  @Builder.Default
  private boolean offPeakOnly = false;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "specific_areas", columnDefinition = "jsonb")
  private String specificAreas; // ["main_gym", "pool", "studio"]

  // Status
  @Column(name = "is_featured")
  @Builder.Default
  private boolean featured = false;

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

