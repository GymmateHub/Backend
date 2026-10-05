package com.gymmate.organisation.internal.domain;

import com.gymmate.shared.constants.GymStatus;
import com.gymmate.shared.domain.TenantEntity;
import com.gymmate.shared.exception.DomainException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.gymmate.shared.domain.Strings;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gym domain entity representing a gym facility/location.
 * Extends TenantJpaEntity for automatic organisation filtering.
 *
 * A Gym belongs to an Organisation (1:N relationship).
 * Members, Classes, Schedules, etc. belong to a specific Gym.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends TenantEntity {

  // Note: organisationId is inherited from TenantJpaEntity

  private String name;

  private String slug;

  private String description;

  // Address fields (flattened from Address value object)
  private String address;

  private String city;

  private String state;

  private String country;

  private String postalCode;

  // Contact information
  private String phone;

  private String email;

  private String contactEmail;

  private String contactPhone;

  private String website;

  private String logoUrl;

  // Business settings
  @Builder.Default
  private 
  String timezone = "UTC";

  @Builder.Default
  private 
  String currency = "USD";

  private 
  String businessHours;

  // Note: Subscription is now at Organisation level, but gyms may have specific
  // features
  @Builder.Default
  private 
  String subscriptionPlan = "starter";

  @Builder.Default
  private GymStatus status = GymStatus.ACTIVE;

  private LocalDateTime subscriptionExpiresAt;

  @Builder.Default
  private 
  Integer maxMembers = 200;

  // Stripe Connect fields for receiving member payments
  private String stripeConnectAccountId;

  @Builder.Default
  private 
  Boolean stripeChargesEnabled = false;

  @Builder.Default
  private 
  Boolean stripePayoutsEnabled = false;

  @Builder.Default
  private 
  Boolean stripeDetailsSubmitted = false;

  private LocalDateTime stripeOnboardingCompletedAt;

  // Features enabled

  @Builder.Default
  private String featuresEnabled = "[]";

  // Status
  @Builder.Default
  private 
  boolean onboardingCompleted = false;

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
    setOrganisationId(organisationId); // Use setter from TenantJpaEntity

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
    if (!Strings.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!Strings.hasText(email)) {
      throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
    }
    if (!Strings.hasText(phone)) {
      throw new DomainException("INVALID_PHONE", "Phone cannot be empty");
    }
  }

  private void validateUpdateInputs(String name, String email, String phone) {
    if (!Strings.hasText(name)) {
      throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
    }
    if (!Strings.hasText(email)) {
      throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
    }
    if (!Strings.hasText(phone)) {
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
