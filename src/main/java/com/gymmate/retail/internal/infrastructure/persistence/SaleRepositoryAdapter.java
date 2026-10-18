package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.retail.internal.application.port.SaleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SaleRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Sale finders live here.
 */
@Component()
@Transactional()
public class SaleRepositoryAdapter extends JpaDomainRepositoryAdapter<Sale, UUID, SaleJpaRepository>
        implements SaleRepository {

    public SaleRepositoryAdapter(SaleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<Sale> findBySaleNumber(String saleNumber) {
        return this.<Optional<Sale>>fromJpa(jpaRepository.findBySaleNumber(saleNumber));
    }

    @Override
    public List<Sale> findByGymId(UUID gymId) {
        return this.<List<Sale>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<Sale> findByGymIdAndStatus(UUID gymId, SaleStatus status) {
        return this.<List<Sale>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<Sale> findByMemberId(UUID memberId) {
        return this.<List<Sale>>fromJpa(jpaRepository.findByMemberId(memberId));
    }

    @Override
    public List<Sale> findByStaffId(UUID staffId) {
        return this.<List<Sale>>fromJpa(jpaRepository.findByStaffId(staffId));
    }

    @Override
    public List<Sale> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<Sale>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public List<Sale> findTodaysSalesByGymId(UUID gymId, LocalDateTime today) {
        return this.<List<Sale>>fromJpa(jpaRepository.findTodaysSalesByGymId(gymId, today));
    }

    @Override
    public long countCompletedByGymId(UUID gymId) {
        return jpaRepository.countCompletedByGymId(gymId);
    }

    @Override
    public BigDecimal sumTotalByGymId(UUID gymId) {
        return jpaRepository.sumTotalByGymId(gymId);
    }

    @Override
    public BigDecimal sumTotalByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.sumTotalByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public BigDecimal sumTodaysTotalByGymId(UUID gymId) {
        return jpaRepository.sumTodaysTotalByGymId(gymId);
    }

    @Override
    public List<Object[]> sumTotalByPaymentTypeAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.sumTotalByPaymentTypeAndDateRange(gymId, startDate, endDate);
    }
}
