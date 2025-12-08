package com.gymmate.user.api.dto;

/**
 * DTO for user profile update requests.
 */
public record UserProfileUpdateRequest(String firstName, String lastName, String phone, String email) {
  public UserProfileUpdateRequest{
    if (email.isBlank())
      throw new IllegalArgumentException("Email is required");
    if (firstName.isBlank())
      throw new IllegalArgumentException("First name is required");
    if (lastName.isBlank())
      throw new IllegalArgumentException("Last name is required");
    if (phone.isBlank())
      throw new IllegalArgumentException("Phone number is required");
  }
}
