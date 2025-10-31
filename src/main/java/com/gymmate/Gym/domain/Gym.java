package com.gymmate.Gym.domain;

import com.gymmate.shared.domain.BaseEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gym domain entity representing a gym facility.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "gyms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Embedded
  private Address address;

  @Column(nullable = false)
  private String contactEmail;

  @Column(nullable = false)
  private String contactPhone;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GymStatus status;

  @Column(columnDefinition = "jsonb")
  private String settings;

  @Column(unique = true)
  private String subdomain;

  @Column(name = "business_info", columnDefinition = "jsonb")
  private String businessInfo;

  @Column(name = "subscription_plan", length = 50)
  private String subscriptionPlan = "starter";

  @Column(name = "subscription_status", length = 20)
  private String subscriptionStatus = "trial";

  @Column(name = "subscription_start_date")
  private LocalDateTime subscriptionStartDate;

  @Column(name = "subscription_end_date")
  private LocalDateTime subscriptionEndDate;

  @Column(name = "trial_ends_at")
  private LocalDateTime trialEndsAt;

  @Column(name = "features_enabled")
  @ElementCollection
  @CollectionTable(name = "gym_features", joinColumns = @JoinColumn(name = "gym_id"))
  private Set<String> featuresEnabled = new HashSet<>();

  @Column(name = "limits", columnDefinition = "jsonb")
  private String limits;

  @Column(name = "billing_info", columnDefinition = "jsonb")
  private String billingInfo;

  public Gym(String name, String description, String contactEmail,
             String contactPhone, UUID ownerId) {
    validateInputs(name, contactEmail, contactPhone, ownerId);

    this.name = name.trim();
    this.description = description != null ? description.trim() : null;
    this.contactEmail = contactEmail.toLowerCase().trim();
    this.contactPhone = contactPhone.trim();
    this.ownerId = ownerId;
    this.status = GymStatus.ACTIVE;
    this.subscriptionPlan = "starter";
    this.subscriptionStatus = "trial";
    this.featuresEnabled = new HashSet<>(Set.of(
        "basic_membership",
        "class_booking",
        "payment_processing",
        "email_notifications"
    ));
    this.settings = "{}";
    this.businessInfo = "{}";
    this.limits = "{}";
    this.billingInfo = "{}";
  }

  public void setSubdomain(String subdomain) {
    if (!StringUtils.hasText(subdomain)) {
      throw new DomainException("INVALID_SUBDOMAIN", "Subdomain cannot be empty");
    }
    this.subdomain = subdomain.toLowerCase().trim();
  }

  public void updateDetails(String name, String description, String contactEmail, String contactPhone) {
    validateUpdateInputs(name, contactEmail, contactPhone);

    this.name = name.trim();
    this.description = description != null ? description.trim() : null;
    this.contactEmail = contactEmail.toLowerCase().trim();
    this.contactPhone = contactPhone.trim();
  }

  public void updateAddress(Address address) {
    this.address = address;
  }

  public void activate() {
    if (this.status == GymStatus.ACTIVE) {
      throw new DomainException("GYM_ALREADY_ACTIVE", "Gym is already active");
    }
    this.status = GymStatus.ACTIVE;
    setActive(true);
  }

  public void deactivate() {
    if (this.status == GymStatus.INACTIVE) {
      throw new DomainException("GYM_ALREADY_INACTIVE", "Gym is already inactive");
    }
    this.status = GymStatus.INACTIVE;
    setActive(false);
  }

  public void suspend() {
    this.status = GymStatus.SUSPENDED;
    setActive(false);
  }

  public boolean isActive() {
    return status == GymStatus.ACTIVE;
  }

  private void validateInputs(String name, String contactEmail, String contactPhone, UUID ownerId) {
    if (!StringUtils.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!StringUtils.hasText(contactEmail)) {
      throw new DomainException("INVALID_CONTACT_EMAIL", "Contact email cannot be empty");
    }
    if (!StringUtils.hasText(contactPhone)) {
      throw new DomainException("INVALID_CONTACT_PHONE", "Contact phone cannot be empty");
    }
    if (ownerId == null) {
      throw new DomainException("INVALID_OWNER", "Owner ID cannot be null");
    }
  }

  private void validateUpdateInputs(String name, String contactEmail, String contactPhone) {
    if (!StringUtils.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!StringUtils.hasText(contactEmail)) {
      throw new DomainException("INVALID_CONTACT_EMAIL", "Contact email cannot be empty");
    }
    if (!StringUtils.hasText(contactPhone)) {
      throw new DomainException("INVALID_CONTACT_PHONE", "Contact phone cannot be empty");
    }
  }

  public void updateSubscription(String plan, LocalDateTime startDate, LocalDateTime endDate) {
    this.subscriptionPlan = plan;
    this.subscriptionStartDate = startDate;
    this.subscriptionEndDate = endDate;
    this.subscriptionStatus = "active";
  }

  public void setTrialPeriod(LocalDateTime trialEndDate) {
    this.trialEndsAt = trialEndDate;
    this.subscriptionStatus = "trial";
  }

  public void updateFeatures(Set<String> features) {
    if (features != null) {
      this.featuresEnabled = new HashSet<>(features);
    }
  }

  public void updateBusinessInfo(String businessInfo) {
    if (StringUtils.hasText(businessInfo)) {
      this.businessInfo = businessInfo;
    }
  }

  public void updateSettings(String settings) {
    if (StringUtils.hasText(settings)) {
      this.settings = settings;
    }
  }

  public void updateLimits(String limits) {
    if (StringUtils.hasText(limits)) {
      this.limits = limits;
    }
  }

  public void updateBillingInfo(String billingInfo) {
    if (StringUtils.hasText(billingInfo)) {
      this.billingInfo = billingInfo;
    }
  }

  public boolean isTrialExpired() {
    return trialEndsAt != null && LocalDateTime.now().isAfter(trialEndsAt);
  }

  public boolean isSubscriptionExpired() {
    return subscriptionEndDate != null && LocalDateTime.now().isAfter(subscriptionEndDate);
  }
}
