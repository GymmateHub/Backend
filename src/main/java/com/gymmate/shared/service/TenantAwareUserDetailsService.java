package com.gymmate.shared.service;

import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAwareUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Load user by username (email) - requires tenant context to be set
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Loading user by email: {}", email);

    // This will be called by Spring Security during authentication
    // At this point, we need to find the user across all tenants for login
    // The actual tenant filtering happens during JWT token generation

    throw new UnsupportedOperationException(
      "Use loadUserByUsernameAndOrganisationId for tenant-aware authentication"
    );
  }

  /**
   * Load user with explicit organisation context (tenant-aware)
   */
  public TenantAwareUserDetails loadUserByUsernameAndOrganisationId(String email, UUID organisationId)
    throws UsernameNotFoundException {
    log.debug("Loading user by email: {} for organisation: {}", email, organisationId);

    User user = userRepository.findByEmailAndOrganisationId(email, organisationId)
      .orElseThrow(() -> new UsernameNotFoundException(
        "User not found with email: " + email + " for organisation: " + organisationId
      ));

    return new TenantAwareUserDetails(
      user.getId(),
      user.getOrganisationId(),
      user.getEmail(),
      user.getPasswordHash(),
      user.getRole().name(),
      user.isActive(),
      user.isEmailVerified()
    );
  }
}
