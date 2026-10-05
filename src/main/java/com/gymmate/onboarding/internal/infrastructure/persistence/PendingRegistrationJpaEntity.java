package com.gymmate.onboarding.internal.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
import com.gymmate.onboarding.internal.domain.PendingRegistration;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link PendingRegistration} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "PendingRegistration")
@Table(name = "pending_registrations", indexes = { @Index(name = "idx_pending_reg_email", columnList = "email"), @Index(name = "idx_pending_reg_expires_at", columnList = "expires_at") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(PendingRegistration.class)
public class PendingRegistrationJpaEntity {

    @Id
    @Column(name = "registration_id", nullable = false, length = 36)
    private String registrationId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "last_otp_sent_at")
    private Instant lastOtpSentAt;

    @Column(name = "otp_attempts", nullable = false)
    private int otpAttempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        if (registrationId == null) {
            registrationId = UUID.randomUUID().toString();
        }
        // Set createdAt in UTC (Instant.now() is always UTC)
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        // Note: expiresAt must be set manually at service level before persisting
    }
}
