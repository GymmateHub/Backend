package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.scheduling.internal.domain.GymArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.scheduling.internal.application.port.GymAreaRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymAreaRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the GymArea finders live here.
 */
@Component()
@Transactional()
public class GymAreaRepositoryAdapter extends JpaDomainRepositoryAdapter<GymArea, UUID, GymAreaJpaRepository>
        implements GymAreaRepository {

    public GymAreaRepositoryAdapter(GymAreaJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
