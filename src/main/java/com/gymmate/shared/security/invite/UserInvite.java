package com.gymmate.shared.security.invite;

import com.gymmate.user.domain.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invitation for ADMIN, TRAINER, or STAFF roles.
 *
 * Invites are created by OWNER/ADMIN users and sent via email.
 * The invitee clicks the link to accept, set their password, and activate their account.
 *
 * Security notes:
 * - Token is hashed before storage (raw token sent via email)
 * - Single-use: status changes to ACCEPTED upon use
 * - Expires after 72 hours by default
 */
@Entity
@Table(name = "user_invites")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ========== Domain Methods ==========

    /**
     * Create a new invite with default 72-hour expiry.
     */
    public static UserInvite create(
            UUID gymId,
            UUID organisationId,
            UUID invitedBy,
            String email,
            UserRole role,
            String firstName,
            String lastName,
            String tokenHash
    ) {
        validateRole(role);

        return UserInvite.builder()
                .gymId(gymId)
                .organisationId(organisationId)
                .invitedBy(invitedBy)
                .email(email.toLowerCase().trim())
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .tokenHash(tokenHash)
                .status(InviteStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(72))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if invite can be accepted.
     */
    public boolean canBeAccepted() {
        return status == InviteStatus.PENDING && !isExpired();
    }

    /**
     * Check if invite has expired based on time.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Mark invite as accepted.
     */
    public void markAccepted() {
        if (!canBeAccepted()) {
            throw new IllegalStateException("Invite cannot be accepted in current state: " + status);
        }
        this.status = InviteStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark invite as expired (called by scheduled job).
     */
    public void markExpired() {
        if (status != InviteStatus.PENDING) {
            return; // Only pending invites can expire
        }
        this.status = InviteStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Revoke the invite (called by OWNER/ADMIN).
     */
    public void revoke() {
        if (status == InviteStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot revoke an already accepted invite");
        }
        this.status = InviteStatus.REVOKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Resend the invite with a new token and reset expiry.
     */
    public void resend(String newTokenHash) {
        if (status == InviteStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot resend an already accepted invite");
        }
        if (status == InviteStatus.REVOKED) {
            throw new IllegalStateException("Cannot resend a revoked invite");
        }
        this.tokenHash = newTokenHash;
        this.status = InviteStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusHours(72);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validate that the role is invite-only (not OWNER, MEMBER, or SUPER_ADMIN).
     */
    private static void validateRole(UserRole role) {
        if (role == UserRole.OWNER || role == UserRole.MEMBER || role == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException(
                "Role " + role + " is not invite-only. Only ADMIN, TRAINER, STAFF can be invited."
            );
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

