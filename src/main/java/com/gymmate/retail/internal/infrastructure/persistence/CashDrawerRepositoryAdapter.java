package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.domain.CashDrawer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.retail.internal.application.port.CashDrawerRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link CashDrawerRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class CashDrawerRepositoryAdapter extends DomainRepositoryAdapter implements CashDrawerRepository {

    private final CashDrawerJpaRepository jpaRepository;

    public CashDrawerRepositoryAdapter(CashDrawerJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CashDrawer> findOpenDrawerByGymId(UUID gymId) {
        return this.<Optional<CashDrawer>>fromJpa(jpaRepository.findOpenDrawerByGymId(gymId));
    }

    @Override
    public List<CashDrawer> findByGymId(UUID gymId) {
        return this.<List<CashDrawer>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<CashDrawer> findByGymIdAndSessionDate(UUID gymId, LocalDate sessionDate) {
        return this.<List<CashDrawer>>fromJpa(jpaRepository.findByGymIdAndSessionDate(gymId, sessionDate));
    }

    @Override
    public List<CashDrawer> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate) {
        return this.<List<CashDrawer>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public Optional<CashDrawer> findOpenDrawerByGymIdAndStaffId(UUID gymId, UUID staffId) {
        return this.<Optional<CashDrawer>>fromJpa(jpaRepository.findOpenDrawerByGymIdAndStaffId(gymId, staffId));
    }

    @Override
    public CashDrawer save(CashDrawer entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<CashDrawer> saveAll(Iterable<CashDrawer> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<CashDrawer> findById(UUID id) {
        return this.<Optional<CashDrawer>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<CashDrawer> findAll() {
        return this.<List<CashDrawer>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<CashDrawer> findAllById(Iterable<UUID> ids) {
        return this.<List<CashDrawer>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(CashDrawer entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<CashDrawer> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public CashDrawer saveAndFlush(CashDrawer entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<CashDrawer> findAll(Pageable pageable) {
        return this.<Page<CashDrawer>>fromJpa(jpaRepository.findAll(pageable));
    }
}
