package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.scheduling.internal.domain.GymClass;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.scheduling.internal.application.port.GymClassRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymClassRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class GymClassRepositoryAdapter extends DomainRepositoryAdapter implements GymClassRepository {

    private final GymClassJpaRepository jpaRepository;

    public GymClassRepositoryAdapter(GymClassJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GymClass save(GymClass gymClass) {
        return save(jpaRepository, gymClass);
    }

    @Override
    public Optional<GymClass> findById(UUID id) {
        return this.<Optional<GymClass>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<GymClass> findByGymId(UUID gymId) {
        return this.<List<GymClass>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<GymClass> findByCategoryId(UUID categoryId) {
        return this.<List<GymClass>>fromJpa(jpaRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<GymClass> findActiveByGymId(UUID gymId) {
        return this.<List<GymClass>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public Optional<GymClass> findByGymIdAndName(UUID gymId, String name) {
        return this.<Optional<GymClass>>fromJpa(jpaRepository.findByGymIdAndName(gymId, name));
    }

    @Override
    public void delete(GymClass gymClass) {
        delete(jpaRepository, gymClass);
    }

    @Override
    public long countByGymId(UUID gymId) {
        return jpaRepository.countByGymId(gymId);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }

    @Override
    public List<GymClass> saveAll(Iterable<GymClass> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<GymClass> findAll() {
        return this.<List<GymClass>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<GymClass> findAllById(Iterable<UUID> ids) {
        return this.<List<GymClass>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<GymClass> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public GymClass saveAndFlush(GymClass entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<GymClass> findAll(Pageable pageable) {
        return this.<Page<GymClass>>fromJpa(jpaRepository.findAll(pageable));
    }
}
