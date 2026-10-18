package com.gymmate.identity.internal.domain;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity with tenant isolation via Hibernate @Filter.
 * Extends TenantJpaEntity which provides:
 * - organisationId field with automatic TenantContext population on persist
 * - @FilterDef/@Filter for automatic tenant-scoped queries
 *
 * NOTE: JwtAuthenticationFilter calls findById() BEFORE tenant context is set,
 * which is safe because TenantFilterAspect only enables the filter when
 * TenantContext.getCurrentTenantId() is non-null.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class User extends TenantEntity {

  private String email;

  private String passwordHash;

  @Builder.Default
  private boolean emailVerified = false;

  private String emailVerificationToken;

  private String passwordResetToken;

  private LocalDateTime passwordResetExpires;

  // Profile
  private String firstName;

  private String lastName;

  private String phone;

  private LocalDate dateOfBirth;

  private String gender; // male, female, other, prefer_not_to_say

  private String profilePhotoUrl;

  // Role & Status

  @Builder.Default
  private UserRole role = UserRole.MEMBER;

  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  // Preferences

  @Builder.Default
  private String preferences = "{}";

  // Security
  @Builder.Default
  private boolean twoFactorEnabled = false;

  private String twoFactorSecret;

  private LocalDateTime lastLoginAt;

  @Builder.Default
  private Integer loginAttempts = 0;

  private LocalDateTime lockedUntil;

  public void updateLastLogin() {
    this.lastLoginAt = LocalDateTime.now();
    this.loginAttempts = 0;
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE && (lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now()));
  }

  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    }
    return email;
  }

  public void updateProfile(String firstName, String lastName, String phone) {
    if (firstName != null) this.firstName = firstName;
    if (lastName != null) this.lastName = lastName;
    if (phone != null) this.phone = phone;
  }

  public void deactivate() {
    this.status = UserStatus.INACTIVE;
  }

  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  public void suspend() {
    this.status = UserStatus.SUSPENDED;
  }

  public void ban() {
    this.status = UserStatus.BANNED;
  }

  public void incrementLoginAttempts() {
    this.loginAttempts++;
    if (this.loginAttempts >= 5) {
      this.lockedUntil = LocalDateTime.now().plusHours(1);
    }
  }

  public void resetLoginAttempts() {
    this.loginAttempts = 0;
    this.lockedUntil = null;
  }

  public void verifyEmail() {
    this.emailVerified = true;
    this.emailVerificationToken = null;
  }
}
