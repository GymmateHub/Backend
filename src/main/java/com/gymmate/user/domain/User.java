package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "users")
public class User extends BaseAuditEntity {

  @Column(name = "organisation_id", nullable = true)
  private UUID organisationId;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "email_verified")
  @Builder.Default
  private boolean emailVerified = false;

  @Column(name = "email_verification_token")
  private String emailVerificationToken;

  @Column(name = "password_reset_token")
  private String passwordResetToken;

  @Column(name = "password_reset_expires")
  private LocalDateTime passwordResetExpires;

  // Profile
  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(length = 20)
  private String phone;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(length = 10)
  private String gender; // male, female, other, prefer_not_to_say

  @Column(name = "profile_photo_url", length = 500)
  private String profilePhotoUrl;

  // Role & Status
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Builder.Default
  private UserRole role = UserRole.MEMBER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  // Preferences
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private String preferences = "{}";

  // Security
  @Column(name = "two_factor_enabled")
  @Builder.Default
  private boolean twoFactorEnabled = false;

  @Column(name = "two_factor_secret")
  private String twoFactorSecret;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "login_attempts")
  @Builder.Default
  private Integer loginAttempts = 0;

  @Column(name = "locked_until")
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


