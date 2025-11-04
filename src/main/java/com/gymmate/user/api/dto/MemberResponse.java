package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for member responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private UUID id;
    private UUID userId;
    private String membershipNumber;
    private LocalDate joinDate;
    private MemberStatus status;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Health information
    private String[] medicalConditions;
    private String[] allergies;
    private String[] medications;
    private String[] fitnessGoals;
    private String experienceLevel;

    // Waiver
    private boolean waiverSigned;
    private LocalDate waiverSignedDate;
    private boolean photoConsent;

    public static MemberResponse fromEntity(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .membershipNumber(member.getMembershipNumber())
                .joinDate(member.getJoinDate())
                .status(member.getStatus())
                .emergencyContactName(member.getEmergencyContactName())
                .emergencyContactPhone(member.getEmergencyContactPhone())
                .emergencyContactRelationship(member.getEmergencyContactRelationship())
                .medicalConditions(member.getMedicalConditions())
                .allergies(member.getAllergies())
                .medications(member.getMedications())
                .fitnessGoals(member.getFitnessGoals())
                .experienceLevel(member.getExperienceLevel())
                .waiverSigned(member.isWaiverSigned())
                .waiverSignedDate(member.getWaiverSignedDate())
                .photoConsent(member.isPhotoConsent())
                .build();
    }
}

