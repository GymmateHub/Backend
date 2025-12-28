package com.gymmate.shared.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, String> {

  Optional<PendingRegistration> findByEmail(String email);

  Optional<PendingRegistration> findByRegistrationId(String registrationId);

  void deleteByExpiresAtBefore(Instant now);

  boolean existsByEmail(String email);
}

