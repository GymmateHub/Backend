package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import com.gymmate.shared.constants.MemberStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for member responses.
 */
public record MemberResponse(
    UUID id,
    UUID userId,
    UUID gymId,
    UUID organisationId,
    String membershipNumber,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate joinDate,
    MemberStatus status,
    // Emergency contact
    String emergencyContactName,
    String emergencyContactPhone,
    String emergencyContactRelationship,
    // Health information
    String[] medicalConditions,
    String[] allergies,
    String[] medications,
    String[] fitnessGoals,
    String experienceLevel,
    // Waiver
    boolean waiverSigned,
    LocalDate waiverSignedDate,
    boolean photoConsent
) {
    public static MemberResponse fromEntity(Member member) {
        return fromEntity(member, null);
    }

    public static MemberResponse fromEntity(Member member, User user) {
        return new MemberResponse(
            member.getId(),
            member.getUserId(),
            member.getGymId(),
            member.getOrganisationId(),
            member.getMembershipNumber(),
            user != null ? user.getFirstName() : null,
            user != null ? user.getLastName() : null,
            user != null ? user.getEmail() : null,
            user != null ? user.getPhone() : null,
            member.getJoinDate(),
            member.getStatus(),
            member.getEmergencyContactName(),
            member.getEmergencyContactPhone(),
            member.getEmergencyContactRelationship(),
            member.getMedicalConditions(),
            member.getAllergies(),
            member.getMedications(),
            member.getFitnessGoals(),
            member.getExperienceLevel(),
            member.isWaiverSigned(),
            member.getWaiverSignedDate(),
            member.isPhotoConsent()
        );
    }
}
