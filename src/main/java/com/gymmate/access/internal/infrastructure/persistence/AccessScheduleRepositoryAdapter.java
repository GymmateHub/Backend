package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.AccessSchedule;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.AccessScheduleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessScheduleRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AccessScheduleRepositoryAdapter extends DomainRepositoryAdapter implements AccessScheduleRepository {

    private final AccessScheduleJpaRepository jpaRepository;

    public AccessScheduleRepositoryAdapter(AccessScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<AccessSchedule> findByMembershipPlanId(UUID membershipPlanId) {
        return this.<List<AccessSchedule>>fromJpa(jpaRepository.findByMembershipPlanId(membershipPlanId));
    }

    @Override
    public AccessSchedule save(AccessSchedule entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AccessSchedule> saveAll(Iterable<AccessSchedule> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AccessSchedule> findById(UUID id) {
        return this.<Optional<AccessSchedule>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AccessSchedule> findAll() {
        return this.<List<AccessSchedule>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AccessSchedule> findAllById(Iterable<UUID> ids) {
        return this.<List<AccessSchedule>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(AccessSchedule entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AccessSchedule> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AccessSchedule saveAndFlush(AccessSchedule entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AccessSchedule> findAll(Pageable pageable) {
        return this.<Page<AccessSchedule>>fromJpa(jpaRepository.findAll(pageable));
    }
}
