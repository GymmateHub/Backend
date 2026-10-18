package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.ExerciseCategoryRepository;
import com.gymmate.health.internal.domain.ExerciseCategory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ExerciseCategoryRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the ExerciseCategory finders live here.
 */
@Component
@Transactional()
public class ExerciseCategoryRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<ExerciseCategory, UUID, ExerciseCategoryJpaRepository>
        implements ExerciseCategoryRepository {

    public ExerciseCategoryRepositoryAdapter(ExerciseCategoryJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
