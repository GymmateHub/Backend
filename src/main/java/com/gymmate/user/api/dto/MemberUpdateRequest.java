package com.gymmate.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating member information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Health information
    private String[] medicalConditions;
    private String[] allergies;
    private String[] medications;

    // Fitness
    private String[] fitnessGoals;
    private String experienceLevel;
}

