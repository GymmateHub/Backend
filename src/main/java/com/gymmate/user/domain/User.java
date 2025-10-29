package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_email_verified")
    private boolean emailVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Override
    protected boolean requiresTenant() {
        return role != UserRole.SUPER_ADMIN;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Convenience mutators used by services
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
}
