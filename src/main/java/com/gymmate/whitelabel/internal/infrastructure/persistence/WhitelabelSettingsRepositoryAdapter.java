package com.gymmate.whitelabel.internal.infrastructure.persistence;

import com.gymmate.whitelabel.internal.application.port.WhitelabelSettingsRepository;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WhitelabelSettingsRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class WhitelabelSettingsRepositoryAdapter extends DomainRepositoryAdapter implements WhitelabelSettingsRepository {

    private final WhitelabelSettingsJpaRepository jpaRepository;

    public WhitelabelSettingsRepositoryAdapter(WhitelabelSettingsJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WhitelabelSettings save(WhitelabelSettings settings) {
        return save(jpaRepository, settings);
    }

    @Override
    public Optional<WhitelabelSettings> findById(UUID id) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByOrganisationIdAndGymIdIsNull(organisationId));
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId));
    }

    @Override
    public Optional<WhitelabelSettings> findByGymId(UUID gymId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<WhitelabelSettings> saveAll(Iterable<WhitelabelSettings> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<WhitelabelSettings> findAll() {
        return this.<List<WhitelabelSettings>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<WhitelabelSettings> findAllById(Iterable<UUID> ids) {
        return this.<List<WhitelabelSettings>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(WhitelabelSettings entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<WhitelabelSettings> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public WhitelabelSettings saveAndFlush(WhitelabelSettings entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<WhitelabelSettings> findAll(Pageable pageable) {
        return this.<Page<WhitelabelSettings>>fromJpa(jpaRepository.findAll(pageable));
    }
}
