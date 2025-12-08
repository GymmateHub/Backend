package com.gymmate.user.api.dto;

    import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;

import java.time.LocalDate;
import java.util.UUID;

    /**
     * DTO for member responses.
     */
    public record MemberResponse(
        UUID id,
        UUID userId,
        String membershipNumber,
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
            return new MemberResponse(
                member.getId(),
                member.getUserId(),
                member.getMembershipNumber(),
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
