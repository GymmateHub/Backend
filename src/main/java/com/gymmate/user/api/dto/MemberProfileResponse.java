package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for the authenticated member's own profile.
 * Combines account (User) and gym membership (Member) data.
 */
public record MemberProfileResponse(
    UUID userId,
    UUID memberId,
    UUID gymId,
    String email,
    String firstName,
    String lastName,
    String phone,
    String profilePhotoUrl,
    String membershipNumber,
    LocalDate joinDate,
    String status,
    boolean waiverSigned,
    String experienceLevel,
    String[] fitnessGoals,
    String emergencyContactName,
    String emergencyContactPhone) {

  public static MemberProfileResponse from(User user, Member member) {
    return new MemberProfileResponse(
        user.getId(),
        member.getId(),
        member.getGymId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhone(),
        user.getProfilePhotoUrl(),
        member.getMembershipNumber(),
        member.getJoinDate(),
        member.getStatus() != null ? member.getStatus().name() : null,
        member.isWaiverSigned(),
        member.getExperienceLevel(),
        member.getFitnessGoals(),
        member.getEmergencyContactName(),
        member.getEmergencyContactPhone());
  }
}
