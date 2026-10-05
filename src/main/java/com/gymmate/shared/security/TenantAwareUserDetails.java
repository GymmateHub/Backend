package com.gymmate.shared.security;

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
    /** Gym context carried by the access token (null when none was selected). */
    private final UUID gymId;

    public TenantAwareUserDetails(UUID userId, UUID organisationId, String email, String password, String role, boolean active, boolean emailVerified) {
        this(userId, organisationId, null, email, password, role, active, emailVerified);
    }

    public TenantAwareUserDetails(UUID userId, UUID organisationId, UUID gymId, String email, String password, String role, boolean active, boolean emailVerified) {
        this.userId = userId;
        this.gymId = gymId;
        this.email = email;
        this.password = password;
        this.organisationId = organisationId;
        this.role = role;
        this.emailVerified = emailVerified;
        this.authorities = buildAuthorities(role);
        this.active = active;
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(String roleName) {
        java.util.Set<GrantedAuthority> auths = new java.util.HashSet<>();
        if (roleName != null) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            if ("GYM_OWNER".equalsIgnoreCase(roleName) || "OWNER".equalsIgnoreCase(roleName)) {
                auths.add(new SimpleGrantedAuthority("ROLE_GYM_OWNER"));
                auths.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if ("SUPER_ADMIN".equalsIgnoreCase(roleName)) {
                auths.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                auths.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                auths.add(new SimpleGrantedAuthority("ROLE_GYM_OWNER"));
            } else if ("MANAGER".equalsIgnoreCase(roleName) || "GYM_MANAGER".equalsIgnoreCase(roleName)) {
                auths.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                auths.add(new SimpleGrantedAuthority("ROLE_GYM_MANAGER"));
            }
        }
        return auths;
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
