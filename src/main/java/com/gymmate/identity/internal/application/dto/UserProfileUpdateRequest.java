package com.gymmate.identity.internal.application.dto;

/**
 * DTO for user profile update requests.
 */
public record UserProfileUpdateRequest(String firstName, String lastName, String phone, String email) {
  public UserProfileUpdateRequest {
    if (firstName == null || firstName.isBlank())
      throw new IllegalArgumentException("First name is required");
    if (lastName == null || lastName.isBlank())
      throw new IllegalArgumentException("Last name is required");
  }
}
