package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.identity.internal.domain.UserInvite;
import lombok.Getter;
import lombok.Setter;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link UserInvite} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "UserInvite")
@Table(name = "user_invites")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(UserInvite.class)
public class UserInviteJpaEntity extends BaseAuditJpaEntity {

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

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
}
