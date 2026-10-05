package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.ExerciseCategoryRepository;
import com.gymmate.health.internal.domain.ExerciseCategory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ExerciseCategoryRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class ExerciseCategoryRepositoryAdapter extends DomainRepositoryAdapter implements ExerciseCategoryRepository {

    private final ExerciseCategoryJpaRepository jpaRepository;

    public ExerciseCategoryRepositoryAdapter(ExerciseCategoryJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ExerciseCategory save(ExerciseCategory category) {
        return save(jpaRepository, category);
    }

    @Override
    public Optional<ExerciseCategory> findById(UUID id) {
        return this.<Optional<ExerciseCategory>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<ExerciseCategory> findAllActive() {
        return this.<List<ExerciseCategory>>fromJpa(jpaRepository.findAllActiveOrderByDisplayOrder());
    }

    @Override
    public Optional<ExerciseCategory> findByName(String name) {
        return this.<Optional<ExerciseCategory>>fromJpa(jpaRepository.findByNameIgnoreCase(name));
    }

    @Override
    public void delete(ExerciseCategory category) {
        category.setActive(false);
        save(jpaRepository, category);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<ExerciseCategory> findAllActiveOrderByDisplayOrder() {
        return this.<List<ExerciseCategory>>fromJpa(jpaRepository.findAllActiveOrderByDisplayOrder());
    }

    @Override
    public Optional<ExerciseCategory> findByNameIgnoreCase(String name) {
        return this.<Optional<ExerciseCategory>>fromJpa(jpaRepository.findByNameIgnoreCase(name));
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<ExerciseCategory> saveAll(Iterable<ExerciseCategory> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ExerciseCategory> findAll() {
        return this.<List<ExerciseCategory>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ExerciseCategory> findAllById(Iterable<UUID> ids) {
        return this.<List<ExerciseCategory>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<ExerciseCategory> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ExerciseCategory saveAndFlush(ExerciseCategory entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ExerciseCategory> findAll(Pageable pageable) {
        return this.<Page<ExerciseCategory>>fromJpa(jpaRepository.findAll(pageable));
    }
}
