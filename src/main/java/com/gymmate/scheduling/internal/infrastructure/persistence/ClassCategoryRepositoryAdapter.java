package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.scheduling.internal.domain.ClassCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.scheduling.internal.application.port.ClassCategoryRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassCategoryRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class ClassCategoryRepositoryAdapter extends DomainRepositoryAdapter implements ClassCategoryRepository {

    private final ClassCategoryJpaRepository jpaRepository;

    public ClassCategoryRepositoryAdapter(ClassCategoryJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClassCategory save(ClassCategory category) {
        return save(jpaRepository, category);
    }

    @Override
    public Optional<ClassCategory> findById(UUID id) {
        return this.<Optional<ClassCategory>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<ClassCategory> findByGymId(UUID gymId) {
        return this.<List<ClassCategory>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<ClassCategory> findActiveByGymId(UUID gymId) {
        return this.<List<ClassCategory>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public Optional<ClassCategory> findByGymIdAndName(UUID gymId, String name) {
        return this.<Optional<ClassCategory>>fromJpa(jpaRepository.findByGymIdAndName(gymId, name));
    }

    @Override
    public void delete(ClassCategory category) {
        delete(jpaRepository, category);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }

    @Override
    public List<ClassCategory> saveAll(Iterable<ClassCategory> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ClassCategory> findAll() {
        return this.<List<ClassCategory>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ClassCategory> findAllById(Iterable<UUID> ids) {
        return this.<List<ClassCategory>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<ClassCategory> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ClassCategory saveAndFlush(ClassCategory entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ClassCategory> findAll(Pageable pageable) {
        return this.<Page<ClassCategory>>fromJpa(jpaRepository.findAll(pageable));
    }
}
