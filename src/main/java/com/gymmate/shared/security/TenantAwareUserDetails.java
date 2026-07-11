package com.gymmate.shared.security;

import com.gymmate.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class TenantAwareUserDetails implements UserDetails {
    private final UUID userId;
    private final String email;
    private final String password;
    private final UUID organisationId;
    private final String role;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public TenantAwareUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.organisationId = user.getOrganisationId();
        this.role = user.getRole().name();
        this.emailVerified = user.isEmailVerified();
        this.authorities = buildAuthorities(user.getRole().name());
        this.active = user.isActive();
    }

    // Alternate constructor used by TenantAwareUserDetailsService
    public TenantAwareUserDetails(UUID userId, UUID organisationId, String email, String password, String role, boolean active, boolean emailVerified) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.organisationId = organisationId;
        this.role = role;
        this.emailVerified = emailVerified;
        this.authorities = buildAuthorities(role);
        this.active = active;
    }

    /**
     * Builds the authority list for a role. "GYM_OWNER" is accepted as a
     * legacy alias for OWNER so sessions or data created before the
     * V11 role migration keep working.
     */
    private static List<GrantedAuthority> buildAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String effectiveRole = "GYM_OWNER".equals(role) ? "OWNER" : role;
        authorities.add(new SimpleGrantedAuthority("ROLE_" + effectiveRole));
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Allow login if active OR if inactive but email not verified (to allow OTP verification)
        return active || !emailVerified;
    }
}
