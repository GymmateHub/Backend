package com.gymmate.shared.security;

import com.gymmate.shared.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Read access to the authenticated principal of the current request.
 *
 * <p>The principal is a {@link TenantAwareUserDetails} populated by identity's JWT
 * authentication filter from a validated access token, so modules no longer need to
 * re-parse the {@code Authorization} header (or depend on identity's JWT service) to
 * learn who is calling.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<TenantAwareUserDetails> principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof TenantAwareUserDetails details) {
            return Optional.of(details);
        }
        return Optional.empty();
    }

    /** @throws AuthException when the request is not authenticated with an access token */
    public static TenantAwareUserDetails requirePrincipal() {
        return principal().orElseThrow(() -> new AuthException("Authentication required"));
    }

    public static UUID requireUserId() {
        return requirePrincipal().getUserId();
    }
}
