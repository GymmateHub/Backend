package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.domain.CashDrawer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.retail.internal.application.port.CashDrawerRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link CashDrawerRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the CashDrawer finders live here.
 */
@Component()
@Transactional()
public class CashDrawerRepositoryAdapter extends JpaDomainRepositoryAdapter<CashDrawer, UUID, CashDrawerJpaRepository>
        implements CashDrawerRepository {

    public CashDrawerRepositoryAdapter(CashDrawerJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
