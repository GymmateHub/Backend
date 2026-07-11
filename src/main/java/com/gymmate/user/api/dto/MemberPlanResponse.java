package com.gymmate.user.api.dto;

import com.gymmate.membership.domain.MembershipPlan;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A membership plan a member can subscribe to, shaped for the mobile app.
 */
public record MemberPlanResponse(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    String billingCycle,
    Integer durationMonths,
    Integer classCredits,
    Integer guestPasses,
    Integer trainerSessions,
    boolean peakHoursAccess,
    boolean featured) {

  public static MemberPlanResponse from(MembershipPlan plan) {
    return new MemberPlanResponse(
        plan.getId(),
        plan.getName(),
        plan.getDescription(),
        plan.getPrice(),
        plan.getBillingCycle(),
        plan.getDurationMonths(),
        plan.getClassCredits(),
        plan.getGuestPasses(),
        plan.getTrainerSessions(),
        plan.isPeakHoursAccess(),
        plan.isFeatured());
  }
}
