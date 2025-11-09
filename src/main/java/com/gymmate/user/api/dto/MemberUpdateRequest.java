package com.gymmate.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating member information.
 */
@Builder
public record MemberUpdateRequest(
    // Emergency contact
  String emergencyContactName,
  String emergencyContactPhone,
  String emergencyContactRelationship,
    // Health information
  String[] medicalConditions,
  String[] allergies,
  String[] medications,
    // Fitness
  String[] fitnessGoals,
  String experienceLevel) { }

