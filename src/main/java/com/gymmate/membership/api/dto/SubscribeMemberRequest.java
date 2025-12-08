package com.gymmate.membership.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for subscribing a member to a plan.
 */
public record SubscribeMemberRequest (

  @NotNull(message = "Member ID is required")
  UUID memberId,

  @NotNull(message = "Plan ID is required")
  UUID planId,

  LocalDate startDate // defaults to today if null
){}
