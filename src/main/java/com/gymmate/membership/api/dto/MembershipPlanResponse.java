package com.gymmate.membership.api.dto;

import com.gymmate.membership.domain.MembershipPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MembershipPlanResponse(
  UUID id,
  UUID gymId,
  String name,
  String description,
  BigDecimal price,
  String billingCycle,
  Integer durationMonths,
  Integer classCredits,
  Integer guestPasses,
  Integer trainerSessions,
  String amenities,
  Boolean peakHoursAccess,
  Boolean offPeakOnly,
  String specificAreas,
  Boolean featured,
  Boolean active,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static MembershipPlanResponse from(MembershipPlan plan) {
    return new MembershipPlanResponse(
      plan.getId(),
      plan.getGymId(),
      plan.getName(),
      plan.getDescription(),
      plan.getPrice(),
      plan.getBillingCycle(),
      plan.getDurationMonths(),
      plan.getClassCredits(),
      plan.getGuestPasses(),
      plan.getTrainerSessions(),
      plan.getAmenities(),
      plan.isPeakHoursAccess(),
      plan.isOffPeakOnly(),
      plan.getSpecificAreas(),
      plan.isFeatured(),
      plan.isActive(),
      plan.getCreatedAt(),
      plan.getUpdatedAt()
    );
  }
}
