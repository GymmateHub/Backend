package com.gymmate.user.domain;

import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User domain entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    public User(String email, String firstName, String lastName, String passwordHash, String phoneNumber, UserRole role) {
        validateInputs(email, firstName, lastName, passwordHash, phoneNumber, role);
        
        this.email = email.toLowerCase().trim();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber.trim();
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        validateProfileInputs(firstName, lastName, phoneNumber);
        
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new DomainException("USER_ALREADY_INACTIVE", "User is already inactive");
        }
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new DomainException("USER_ALREADY_ACTIVE", "User is already active");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private void validateInputs(String email, String firstName, String lastName, String passwordHash, String phoneNumber, UserRole role) {
        if (!StringUtils.hasText(email)) {
            throw new DomainException("INVALID_EMAIL", "Email cannot be empty");
        }
        if (!StringUtils.hasText(firstName)) {
            throw new DomainException("INVALID_FIRST_NAME", "First name cannot be empty");
        }
        if (!StringUtils.hasText(lastName)) {
            throw new DomainException("INVALID_LAST_NAME", "Last name cannot be empty");
        }
        if (!StringUtils.hasText(passwordHash)) {
            throw new DomainException("INVALID_PASSWORD", "Password hash cannot be empty");
        }
        if (!StringUtils.hasText(phoneNumber)) {
            throw new DomainException("INVALID_PHONE", "Phone number cannot be empty");
        }
        if (role == null) {
            throw new DomainException("INVALID_ROLE", "User role cannot be null");
        }
    }

    private void validateProfileInputs(String firstName, String lastName, String phoneNumber) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}