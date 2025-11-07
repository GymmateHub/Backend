package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "members")
public class Member extends BaseAuditEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "membership_number", unique = true, length = 50)
  private String membershipNumber;

  @Column(name = "join_date")
  @Builder.Default
  private LocalDate joinDate = LocalDate.now();

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  @Builder.Default
  private MemberStatus status = MemberStatus.ACTIVE;

  // Emergency contact
  @Column(name = "emergency_contact_name")
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone", length = 20)
  private String emergencyContactPhone;

  @Column(name = "emergency_contact_relationship", length = 50)
  private String emergencyContactRelationship;

  // Health information
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "medical_conditions", columnDefinition = "text[]")
  private String[] medicalConditions;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] allergies;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] medications;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "fitness_goals", columnDefinition = "text[]")
  private String[] fitnessGoals;

  @Column(name = "experience_level", length = 20)
  private String experienceLevel; // beginner, intermediate, advanced

  // Preferences
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "preferred_workout_times", columnDefinition = "jsonb")
  private String preferredWorkoutTimes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "communication_preferences", columnDefinition = "jsonb")
  private String communicationPreferences;

  // Waiver & agreements
  @Column(name = "waiver_signed")
  @Builder.Default
  private boolean waiverSigned = false;

  @Column(name = "waiver_signed_date")
  private LocalDate waiverSignedDate;

  @Column(name = "photo_consent")
  @Builder.Default
  private boolean photoConsent = false;

  public void signWaiver() {
    this.waiverSigned = true;
    this.waiverSignedDate = LocalDate.now();
  }

  public void updateEmergencyContact(String name, String phone, String relationship) {
    this.emergencyContactName = name;
    this.emergencyContactPhone = phone;
    this.emergencyContactRelationship = relationship;
  }

  public void activate() {
    this.status = MemberStatus.ACTIVE;
  }

  public void suspend() {
    this.status = MemberStatus.SUSPENDED;
  }

  public void cancel() {
    this.status = MemberStatus.CANCELLED;
  }

  public boolean isActive() {
    return this.status == MemberStatus.ACTIVE;
  }
}

