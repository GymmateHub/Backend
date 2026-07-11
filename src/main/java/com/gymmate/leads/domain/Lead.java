package com.gymmate.leads.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lead entity representing a prospective member for a gym.
 * Extends GymScopedEntity: scoped to both an organisation (tenant) and a gym,
 * so tenants managing multiple gyms keep separate lead pipelines per gym.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "leads")
public class Lead extends GymScopedEntity {

  // Note: organisationId and gymId are inherited from GymScopedEntity

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(length = 255)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(length = 100)
  private String source; // walk-in, website, referral, social media, etc.

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  @Builder.Default
  private LeadStatus status = LeadStatus.NEW;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "assigned_to")
  private UUID assignedTo; // staff/user responsible for following up

  @Column(name = "follow_up_date")
  private LocalDate followUpDate;

  @Column(name = "converted_at")
  private LocalDateTime convertedAt;

  @Column(name = "converted_member_id")
  private UUID convertedMemberId;

  // ===== Domain behaviour =====

  public void updateStatus(LeadStatus newStatus) {
    if (this.status == LeadStatus.CONVERTED) {
      throw new IllegalStateException("A converted lead cannot change status");
    }
    this.status = newStatus;
    if (newStatus == LeadStatus.CONVERTED && this.convertedAt == null) {
      this.convertedAt = LocalDateTime.now();
    }
  }

  public void convert(UUID memberId) {
    if (this.status == LeadStatus.CONVERTED) {
      throw new IllegalStateException("Lead has already been converted");
    }
    this.status = LeadStatus.CONVERTED;
    this.convertedAt = LocalDateTime.now();
    this.convertedMemberId = memberId;
  }

  public boolean isConverted() {
    return this.status == LeadStatus.CONVERTED;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
