package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.scheduling.internal.domain.GymArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.scheduling.internal.application.port.GymAreaRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymAreaRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class GymAreaRepositoryAdapter extends DomainRepositoryAdapter implements GymAreaRepository {

    private final GymAreaJpaRepository jpaRepository;

    public GymAreaRepositoryAdapter(GymAreaJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GymArea save(GymArea area) {
        return save(jpaRepository, area);
    }

    @Override
    public Optional<GymArea> findById(UUID id) {
        return this.<Optional<GymArea>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<GymArea> findByGymId(UUID gymId) {
        return this.<List<GymArea>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public Optional<GymArea> findByGymIdAndName(UUID gymId, String name) {
        return this.<Optional<GymArea>>fromJpa(jpaRepository.findByGymIdAndName(gymId, name));
    }

    @Override
    public void delete(GymArea area) {
        delete(jpaRepository, area);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }

    @Override
    public List<GymArea> saveAll(Iterable<GymArea> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<GymArea> findAll() {
        return this.<List<GymArea>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<GymArea> findAllById(Iterable<UUID> ids) {
        return this.<List<GymArea>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<GymArea> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public GymArea saveAndFlush(GymArea entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<GymArea> findAll(Pageable pageable) {
        return this.<Page<GymArea>>fromJpa(jpaRepository.findAll(pageable));
    }
}
