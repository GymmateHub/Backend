package com.gymmate.user.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for a member updating their own profile.
 * All fields optional (partial update).
 */
public record MemberSelfProfileUpdateRequest(
    @Size(min = 2, max = 100) String firstName,
    @Size(min = 2, max = 100) String lastName,
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid") String phone,
    @Size(max = 200) String emergencyContactName,
    @Size(max = 20) String emergencyContactPhone,
    @Size(max = 100) String emergencyContactRelationship) {
}
