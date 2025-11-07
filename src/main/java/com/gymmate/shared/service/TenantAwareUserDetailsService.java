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
      "Use loadUserByUsernameAndGymId for tenant-aware authentication"
    );
  }

  /**
   * Load user with explicit gym context (tenant-aware)
   */
  public TenantAwareUserDetails loadUserByUsernameAndGymId(String email, UUID gymId)
    throws UsernameNotFoundException {
    log.debug("Loading user by email: {} for gym: {}", email, gymId);

    User user = userRepository.findByEmailAndGymId(email, gymId)
      .orElseThrow(() -> new UsernameNotFoundException(
        "User not found with email: " + email + " for gym: " + gymId
      ));

    return new TenantAwareUserDetails(
      user.getId(),
      user.getGymId(),
      user.getEmail(),
      user.getPasswordHash(),
      user.getRole().name(),
      user.isActive(),
      user.isEmailVerified()
    );
  }
}
