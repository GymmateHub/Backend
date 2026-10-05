package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link User} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "User")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(User.class)
public class UserJpaEntity extends TenantJpaEntity {

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email_verified")
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
    private String // male, female, other, prefer_not_to_say
    gender;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // Role & Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    // Preferences
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String preferences = "{}";

    // Security
    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
}
