package com.gymmate.identity.internal.domain;

import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserInvite extends BaseAuditEntity {

    private UUID gymId;

    private UUID organisationId;

    private UUID invitedBy;

    private String email;

    private UserRole role;

    private String firstName;

    private String lastName;

    private String token;

    private String tokenHash;

    @Builder.Default
    private InviteStatus status = InviteStatus.PENDING;

    private LocalDateTime expiresAt;

    private LocalDateTime acceptedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
