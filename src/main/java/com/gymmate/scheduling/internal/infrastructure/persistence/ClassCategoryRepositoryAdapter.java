package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.scheduling.internal.domain.ClassCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.scheduling.internal.application.port.ClassCategoryRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassCategoryRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the ClassCategory finders live here.
 */
@Component()
@Transactional()
public class ClassCategoryRepositoryAdapter extends JpaDomainRepositoryAdapter<ClassCategory, UUID, ClassCategoryJpaRepository>
        implements ClassCategoryRepository {

    public ClassCategoryRepositoryAdapter(ClassCategoryJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
