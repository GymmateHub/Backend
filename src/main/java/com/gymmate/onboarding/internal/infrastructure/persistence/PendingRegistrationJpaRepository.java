package com.gymmate.onboarding.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface PendingRegistrationJpaRepository extends JpaRepository<PendingRegistrationJpaEntity, String> {

    Optional<PendingRegistrationJpaEntity> findByEmail(String email);

    Optional<PendingRegistrationJpaEntity> findByRegistrationId(String registrationId);

    void deleteByExpiresAtBefore(Instant now);

    boolean existsByEmail(String email);
}
