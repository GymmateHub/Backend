package com.gymmate.membership.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for freezing a membership.
 */
public record FreezeMembershipRequest (

  @NotNull(message = "Freeze until date is required")
  @FutureOrPresent(message = "Freeze date must be in the future or present")
  LocalDate freezeUntil,
  @NotNull(message = "Reason is required")
  @Size(max = 255, message = "Reason must not exceed 255 characters")
  String reason
){}

