package com.gymmate.onboarding.internal.infrastructure.persistence;

import com.gymmate.onboarding.internal.domain.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.onboarding.internal.application.port.PendingRegistrationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PendingRegistrationRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class PendingRegistrationRepositoryAdapter extends DomainRepositoryAdapter implements PendingRegistrationRepository {

    private final PendingRegistrationJpaRepository jpaRepository;

    public PendingRegistrationRepositoryAdapter(PendingRegistrationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public PendingRegistration save(PendingRegistration entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<PendingRegistration> saveAll(Iterable<PendingRegistration> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<PendingRegistration> findById(String id) {
        return this.<Optional<PendingRegistration>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<PendingRegistration> findAll() {
        return this.<List<PendingRegistration>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<PendingRegistration> findAllById(Iterable<String> ids) {
        return this.<List<PendingRegistration>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(PendingRegistration entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<PendingRegistration> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public PendingRegistration saveAndFlush(PendingRegistration entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<PendingRegistration> findAll(Pageable pageable) {
        return this.<Page<PendingRegistration>>fromJpa(jpaRepository.findAll(pageable));
    }
}
