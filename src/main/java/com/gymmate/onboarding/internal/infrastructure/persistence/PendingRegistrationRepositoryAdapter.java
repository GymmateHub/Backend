package com.gymmate.onboarding.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.onboarding.internal.domain.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import com.gymmate.onboarding.internal.application.port.PendingRegistrationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PendingRegistrationRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the PendingRegistration finders live here.
 */
@Component()
@Transactional()
public class PendingRegistrationRepositoryAdapter extends JpaDomainRepositoryAdapter<PendingRegistration, String, PendingRegistrationJpaRepository>
        implements PendingRegistrationRepository {

    public PendingRegistrationRepositoryAdapter(PendingRegistrationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<PendingRegistration> findByEmail(String email) {
        return this.<Optional<PendingRegistration>>fromJpa(jpaRepository.findByEmail(email));
    }

    @Override
    public Optional<PendingRegistration> findByRegistrationId(String registrationId) {
        return this.<Optional<PendingRegistration>>fromJpa(jpaRepository.findByRegistrationId(registrationId));
    }

    @Override
    public void deleteByExpiresAtBefore(Instant now) {
        jpaRepository.deleteByExpiresAtBefore(now);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
