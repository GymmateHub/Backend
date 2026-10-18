package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.scheduling.internal.domain.GymClass;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.scheduling.internal.application.port.GymClassRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymClassRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the GymClass finders live here.
 */
@Component()
@Transactional()
public class GymClassRepositoryAdapter extends JpaDomainRepositoryAdapter<GymClass, UUID, GymClassJpaRepository>
        implements GymClassRepository {

    public GymClassRepositoryAdapter(GymClassJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public long countByGymId(UUID gymId) {
        return jpaRepository.countByGymId(gymId);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
