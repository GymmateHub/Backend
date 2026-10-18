package com.gymmate.identity.internal.domain;

import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Member entity representing a gym member.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 *
 * A member belongs to a specific gym but the User they reference
 * belongs to the organisation (allowing multi-gym membership if org allows).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Member extends GymScopedEntity {

  private UUID userId;

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String membershipNumber;

  @Builder.Default
  private LocalDate joinDate = LocalDate.now();

  @Builder.Default
  private MemberStatus status = MemberStatus.ACTIVE;

  // Emergency contact
  private String emergencyContactName;

  private String emergencyContactPhone;

  private String emergencyContactRelationship;

  // Health information
  private String[] medicalConditions;

  private String[] allergies;

  private String[] medications;

  private String[] fitnessGoals;

  private String experienceLevel; // beginner, intermediate, advanced

  // Preferences
  private String preferredWorkoutTimes;

  private String communicationPreferences;

  // Waiver & agreements
  @Builder.Default
  private boolean waiverSigned = false;

  private LocalDate waiverSignedDate;

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
