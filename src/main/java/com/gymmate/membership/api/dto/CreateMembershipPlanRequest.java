package com.gymmate.membership.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for creating a membership plan.
 */
public record CreateMembershipPlanRequest(
  @NotBlank(message = "Plan name is required")
  @Size(max = 100, message = "Plan name must not exceed 100 characters")
  String name,

  String description,

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", message = "Price must be non-negative")
  BigDecimal price,

  @NotBlank(message = "Billing cycle is required")
  @Pattern(regexp = "monthly|quarterly|yearly|annual|lifetime",
    message = "Billing cycle must be one of: monthly, quarterly, yearly, annual, lifetime")
  String billingCycle,

  @Min(value = 1, message = "Duration must be at least 1 month")
  Integer durationMonths, // null for lifetime

  @Min(value = 0, message = "Class credits cannot be negative")
  Integer classCredits, // null for unlimited

  @Min(value = 0, message = "Guest passes cannot be negative")
  Integer guestPasses,

  @Min(value = 0, message = "Trainer sessions cannot be negative")
  Integer trainerSessions,

  Boolean peakHoursAccess,

  Boolean offPeakOnly,

  Boolean featured,

  String amenities, // JSON string

  String specificAreas // JSON string
) {}
