package com.gymmate.shared.security;

import com.gymmate.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
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
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
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
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        this.active = active;
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
