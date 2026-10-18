package com.gymmate.identity.internal.infrastructure.security;

import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.security.TenantAwareUserDetails;

import java.util.UUID;

/** Builds the shared security principal from identity's {@link User} aggregate. */
final class SecurityPrincipals {

    private SecurityPrincipals() {
    }

    static TenantAwareUserDetails of(User user) {
        return of(user, null);
    }

    static TenantAwareUserDetails of(User user, UUID gymId) {
        return new TenantAwareUserDetails(
                user.getId(),
                user.getOrganisationId(),
                gymId,
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.isActive(),
                user.isEmailVerified());
    }
}
