package com.gymmate.shared.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class TenantAwareUserDetails implements UserDetails {

  private final UUID userId;
  private final UUID gymId;  // This is the tenant ID
  private final String email;
  private final String password;
  private final String role;
  private final boolean active;
  private final boolean emailVerified;

  public TenantAwareUserDetails(UUID userId,
                                UUID gymId,
                                String email,
                                String password,
                                String role,
                                boolean active,
                                boolean emailVerified) {
    this.userId = userId;
    this.gymId = gymId;
    this.email = email;
    this.password = password;
    this.role = role;
    this.active = active;
    this.emailVerified = emailVerified;
  }

  public UUID getTenantId() {
    return gymId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
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
    return active;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
