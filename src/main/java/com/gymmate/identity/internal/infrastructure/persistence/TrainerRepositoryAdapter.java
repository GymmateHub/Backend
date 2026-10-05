package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.identity.internal.domain.Trainer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.TrainerRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link TrainerRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class TrainerRepositoryAdapter extends DomainRepositoryAdapter implements TrainerRepository {

    private final TrainerJpaRepository jpaRepository;

    public TrainerRepositoryAdapter(TrainerJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Trainer> findByUserId(UUID userId) {
        return this.<Optional<Trainer>>fromJpa(jpaRepository.findByUserId(userId));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public List<Trainer> findByOrganisationId(UUID organisationId) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Trainer> findActiveAndAcceptingClientsByOrganisationId(UUID organisationId) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findActiveAndAcceptingClientsByOrganisationId(organisationId));
    }

    @Override
    public List<Trainer> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByOrganisationIdAndEmploymentType(organisationId, employmentType));
    }

    @Override
    public List<Trainer> findActiveAndAcceptingClients() {
        return this.<List<Trainer>>fromJpa(jpaRepository.findActiveAndAcceptingClients());
    }

    @Override
    public List<Trainer> findByAcceptingClients(boolean accepting) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByAcceptingClients(accepting));
    }

    @Override
    public List<Trainer> findByEmploymentType(String employmentType) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByEmploymentType(employmentType));
    }

    @Override
    public Trainer save(Trainer entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Trainer> saveAll(Iterable<Trainer> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Trainer> findById(UUID id) {
        return this.<Optional<Trainer>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Trainer> findAll() {
        return this.<List<Trainer>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Trainer> findAllById(Iterable<UUID> ids) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(Trainer entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Trainer> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Trainer saveAndFlush(Trainer entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Trainer> findAll(Pageable pageable) {
        return this.<Page<Trainer>>fromJpa(jpaRepository.findAll(pageable));
    }
}
