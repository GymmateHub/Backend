package com.gymmate.user.api.dto;

import java.util.UUID;

/**
 * DTO for creating a new member.
 * Supports both creating for an existing user (by userId) or registering with details.
 */
public record MemberCreateRequest(
    UUID userId,
    UUID gymId,
    String membershipNumber,
    String email,
    String firstName,
    String lastName,
    String phone
) {
    public MemberCreateRequest(UUID userId, UUID gymId, String membershipNumber) {
        this(userId, gymId, membershipNumber, null, null, null, null);
    }
}
