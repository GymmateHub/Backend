package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.AccessSchedule;
import java.util.List;
import java.util.UUID;
import com.gymmate.access.internal.application.port.AccessScheduleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessScheduleRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AccessSchedule finders live here.
 */
@Component()
@Transactional()
public class AccessScheduleRepositoryAdapter extends JpaDomainRepositoryAdapter<AccessSchedule, UUID, AccessScheduleJpaRepository>
        implements AccessScheduleRepository {

    public AccessScheduleRepositoryAdapter(AccessScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<AccessSchedule> findByMembershipPlanId(UUID membershipPlanId) {
        return this.<List<AccessSchedule>>fromJpa(jpaRepository.findByMembershipPlanId(membershipPlanId));
    }
}
