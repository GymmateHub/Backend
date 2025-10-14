package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * User domain entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name= "is_email_verified", nullable = false)
    private boolean isEmailVerified;

  {
    isEmailVerified = false;
  }

  @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public User(String email, String firstName, String lastName, String passwordHash,
                String phoneNumber, UserRole role) {
        validateInputs(email, firstName, lastName, passwordHash, phoneNumber);
        this.email = email.toLowerCase().trim();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber.trim();
        this.role = role;
        this.status = UserStatus.ACTIVE;
        setActive(true);
        validateUserTenancy();
    }

    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        validateProfileUpdate(firstName, lastName, phoneNumber);
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.phoneNumber = phoneNumber.trim();
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new DomainException("USER_ALREADY_INACTIVE", "User is already inactive");
        }
        this.status = UserStatus.INACTIVE;
        setActive(false);
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new DomainException("USER_ALREADY_ACTIVE", "User is already active");
        }
        this.status = UserStatus.ACTIVE;
        setActive(true);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    private void validateInputs(String email, String firstName, String lastName,
                              String passwordHash, String phoneNumber) {
        if (!StringUtils.hasText(email)) {
            throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
        }
        validateProfileUpdate(firstName, lastName, phoneNumber);
        if (!StringUtils.hasText(passwordHash)) {
            throw new DomainException("INVALID_PASSWORD", "Password hash cannot be empty");
        }
    }

    private void validateProfileUpdate(String firstName, String lastName, String phoneNumber) {
        if (!StringUtils.hasText(firstName)) {
            throw new DomainException("INVALID_FIRST_NAME", "First name cannot be empty");
        }
        if (!StringUtils.hasText(lastName)) {
            throw new DomainException("INVALID_LAST_NAME", "Last name cannot be empty");
        }
        if (!StringUtils.hasText(phoneNumber)) {
            throw new DomainException("INVALID_PHONE", "Phone number cannot be empty");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        validateUserTenancy();
    }

    private void validateUserTenancy() {
        // Ensure gym owners don't have a gymId
        if (role == UserRole.GYM_OWNER && getGymId() != null) {
            throw new DomainException("INVALID_GYM_OWNER",
                "Gym owners cannot be associated with a specific gym");
        }

        // Ensure members and staff have a gymId
        if ((role == UserRole.MEMBER || role == UserRole.STAFF) && getGymId() == null) {
            throw new DomainException("INVALID_USER",
                "Members and staff must be associated with a gym");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return getId() != null && getId().equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
