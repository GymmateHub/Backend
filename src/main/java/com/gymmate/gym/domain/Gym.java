package com.gymmate.gym.domain;

import com.gymmate.shared.domain.TenantEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gym domain entity representing a gym facility/location.
 * Extends TenantEntity for automatic organisation filtering.
 *
 * A Gym belongs to an Organisation (1:N relationship).
 * Members, Classes, Schedules, etc. belong to a specific Gym.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "gyms")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends TenantEntity {

  // Note: organisationId is inherited from TenantEntity

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false, length = 100)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  // Address fields (flattened from Address value object)
  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(length = 100)
  private String city;

  @Column(length = 50)
  private String state;

  @Column(length = 50)
  private String country;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  // Contact information
  @Column(length = 20)
  private String phone;

  @Column
  private String email;

  @Column(name = "contact_email")
  private String contactEmail;

  @Column(name = "contact_phone", length = 20)
  private String contactPhone;

  @Column
  private String website;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  // Business settings
  @Column(length = 50)
  @Builder.Default
  private String timezone = "UTC";

  @Column(length = 3)
  @Builder.Default
  private String currency = "USD";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "business_hours", columnDefinition = "jsonb")
  private String businessHours;

  // Note: Subscription is now at Organisation level, but gyms may have specific
  // features
  @Column(name = "subscription_plan", length = 50)
  @Builder.Default
  private String subscriptionPlan = "starter";

  @Enumerated(EnumType.STRING)
  @Column(name = "subscription_status", length = 20)
  @Builder.Default
  private GymStatus status = GymStatus.ACTIVE;

  @Column(name = "subscription_expires_at")
  private LocalDateTime subscriptionExpiresAt;

  @Column(name = "max_members")
  @Builder.Default
  private Integer maxMembers = 200;

  // Stripe Connect fields for receiving member payments
  @Column(name = "stripe_connect_account_id")
  private String stripeConnectAccountId;

  @Column(name = "stripe_charges_enabled")
  @Builder.Default
  private Boolean stripeChargesEnabled = false;

  @Column(name = "stripe_payouts_enabled")
  @Builder.Default
  private Boolean stripePayoutsEnabled = false;

  @Column(name = "stripe_details_submitted")
  @Builder.Default
  private Boolean stripeDetailsSubmitted = false;

  @Column(name = "stripe_onboarding_completed_at")
  private LocalDateTime stripeOnboardingCompletedAt;

  // Features enabled
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "features_enabled", columnDefinition = "jsonb")
  @Builder.Default
  private String featuresEnabled = "[]";

  // Status
  @Column(name = "onboarding_completed")
  @Builder.Default
  private boolean onboardingCompleted = false;

  /**
   * Create a new Gym with organisation context.
   * This is the preferred constructor for multi-tenant architecture.
   */
  public Gym(String name, String description, String contactEmail, String contactPhone, UUID organisationId) {
    validateInputs(name, contactEmail, contactPhone);

    this.name = name.trim();
    this.slug = generateSlug(name);
    this.description = description;
    this.contactEmail = contactEmail.toLowerCase().trim();
    this.email = contactEmail.toLowerCase().trim();
    this.contactPhone = contactPhone.trim();
    this.phone = contactPhone.trim();
    setOrganisationId(organisationId); // Use setter from TenantEntity

    // Initialize defaults
    this.timezone = "UTC";
    this.currency = "USD";
    this.subscriptionPlan = "starter";
    this.status = GymStatus.ACTIVE;
    this.maxMembers = 200;
    this.stripeChargesEnabled = false;
    this.stripePayoutsEnabled = false;
    this.stripeDetailsSubmitted = false;
    this.featuresEnabled = "[]";
    this.onboardingCompleted = false;
  }

  /**
   * Static factory method to create a default gym for a new organisation.
   */
  public static Gym createDefault(String orgName, String contactEmail, String contactPhone, UUID organisationId) {
    String gymName = orgName.contains("Organization")
        ? orgName.replace(" Organization", " - Main Location")
        : orgName + " - Main Location";

    Gym gym = new Gym(gymName, "Default gym location", contactEmail, contactPhone, organisationId);
    gym.setOnboardingCompleted(false);
    return gym;
  }

  private String generateSlug(String name) {
    String baseSlug = name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .trim();
    // Add timestamp suffix to ensure uniqueness
    return baseSlug + "-" + System.currentTimeMillis() % 100000;
  }

  public void updateDetails(String name, String description, String contactEmail, String contactPhone, String website) {
    validateUpdateInputs(name, contactEmail, contactPhone);
    this.name = name.trim();
    this.description = description;
    this.contactEmail = contactEmail.toLowerCase().trim();
    this.email = contactEmail.toLowerCase().trim();
    this.contactPhone = contactPhone.trim();
    this.phone = contactPhone.trim();
    if (website != null) {
      this.website = website.trim();
    }
  }

  public void updateAddress(String address, String city, String state, String country, String postalCode) {
    this.address = address;
    this.city = city;
    this.state = state;
    this.country = country;
    this.postalCode = postalCode;
  }

  public void activate() {
    if (this.status == GymStatus.ACTIVE) {
      throw new DomainException("GYM_ALREADY_ACTIVE", "Gym is already active");
    }
    this.status = GymStatus.ACTIVE;
    setActive(true);
  }

  public void deactivate() {
    this.status = GymStatus.SUSPENDED;
    setActive(false);
  }

  public void suspend() {
    this.status = GymStatus.SUSPENDED;
    setActive(false);
  }

  public void cancel() {
    this.status = GymStatus.CANCELLED;
    setActive(false);
  }

  public boolean isActive() {
    return status == GymStatus.ACTIVE;
  }

  private void validateInputs(String name, String email, String phone) {
    if (!StringUtils.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!StringUtils.hasText(email)) {
      throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
    }
    if (!StringUtils.hasText(phone)) {
      throw new DomainException("INVALID_PHONE", "Phone cannot be empty");
    }
  }

  private void validateUpdateInputs(String name, String email, String phone) {
    if (!StringUtils.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!StringUtils.hasText(email)) {
      throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
    }
    if (!StringUtils.hasText(phone)) {
      throw new DomainException("INVALID_PHONE", "Phone cannot be empty");
    }
  }

  public void updateSubscription(String plan, LocalDateTime expiresAt) {
    this.subscriptionPlan = plan;
    this.subscriptionExpiresAt = expiresAt;
    this.status = GymStatus.ACTIVE;
  }

  public void completeOnboarding() {
    this.onboardingCompleted = true;
  }

  public boolean isSubscriptionExpired() {
    return subscriptionExpiresAt != null && LocalDateTime.now().isAfter(subscriptionExpiresAt);
  }
}
