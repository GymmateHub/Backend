package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new member.
 */

public record MemberCreateRequest(UUID userId, String membershipNumber) {
  public MemberCreateRequest{
    if (userId == null)
      throw new IllegalArgumentException("User ID is required");
    if (membershipNumber == null)
      throw new IllegalArgumentException("Membership number is required");
    if (membershipNumber.isBlank())
      throw new IllegalArgumentException("Membership number is required");
  }
}

