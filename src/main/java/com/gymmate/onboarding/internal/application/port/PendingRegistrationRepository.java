package com.gymmate.onboarding.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.onboarding.internal.domain.PendingRegistration;

import java.time.Instant;
import java.util.Optional;

public interface PendingRegistrationRepository extends DomainRepository<PendingRegistration, String> {

  Optional<PendingRegistration> findByEmail(String email);

  Optional<PendingRegistration> findByRegistrationId(String registrationId);

  void deleteByExpiresAtBefore(Instant now);

  boolean existsByEmail(String email);
}

