package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.AccessPoint;
import java.util.List;
import java.util.UUID;
import com.gymmate.access.internal.application.port.AccessPointRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessPointRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AccessPoint finders live here.
 */
@Component()
@Transactional()
public class AccessPointRepositoryAdapter extends JpaDomainRepositoryAdapter<AccessPoint, UUID, AccessPointJpaRepository>
        implements AccessPointRepository {

    public AccessPointRepositoryAdapter(AccessPointJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<AccessPoint> findByGymId(UUID gymId) {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<AccessPoint> findByOrganisationId(UUID organisationId) {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }
}
