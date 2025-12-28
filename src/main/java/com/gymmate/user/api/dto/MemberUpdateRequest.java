package com.gymmate.user.api.dto;

/**
 * DTO for updating member information.
 */
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
