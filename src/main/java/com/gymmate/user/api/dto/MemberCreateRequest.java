package com.gymmate.user.api.dto;

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
