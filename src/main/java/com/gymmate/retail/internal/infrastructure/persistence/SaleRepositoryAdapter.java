package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.retail.internal.application.port.SaleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SaleRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SaleRepositoryAdapter extends DomainRepositoryAdapter implements SaleRepository {

    private final SaleJpaRepository jpaRepository;

    public SaleRepositoryAdapter(SaleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public Sale save(Sale entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Sale> saveAll(Iterable<Sale> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Sale> findById(UUID id) {
        return this.<Optional<Sale>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Sale> findAll() {
        return this.<List<Sale>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Sale> findAllById(Iterable<UUID> ids) {
        return this.<List<Sale>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(Sale entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Sale> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Sale saveAndFlush(Sale entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Sale> findAll(Pageable pageable) {
        return this.<Page<Sale>>fromJpa(jpaRepository.findAll(pageable));
    }
}
