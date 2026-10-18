package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.organisation.internal.domain.Organisation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.organisation.internal.application.port.OrganisationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link OrganisationRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Organisation finders live here.
 */
@Component()
@Transactional()
public class OrganisationRepositoryAdapter extends JpaDomainRepositoryAdapter<Organisation, UUID, OrganisationJpaRepository>
        implements OrganisationRepository {

    public OrganisationRepositoryAdapter(OrganisationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<Organisation> findBySlug(String slug) {
        return this.<Optional<Organisation>>fromJpa(jpaRepository.findBySlug(slug));
    }

    @Override
    public Optional<Organisation> findByOwnerUserId(UUID ownerUserId) {
        return this.<Optional<Organisation>>fromJpa(jpaRepository.findByOwnerUserId(ownerUserId));
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public List<Organisation> findAllActive() {
        return this.<List<Organisation>>fromJpa(jpaRepository.findAllActive());
    }

    @Override
    public List<Organisation> findBySubscriptionStatus(String status) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findBySubscriptionStatus(status));
    }

    @Override
    public List<Organisation> findTrialsEndingBefore(LocalDateTime date) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findTrialsEndingBefore(date));
    }

    @Override
    public List<Organisation> findSubscriptionsExpiringBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findSubscriptionsExpiringBetween(start, end));
    }

    @Override
    public long countActive() {
        return jpaRepository.countActive();
    }

    @Override
    public long countBySubscriptionStatus(String status) {
        return jpaRepository.countBySubscriptionStatus(status);
    }

    @Override
    public List<Organisation> findTrialsEndingBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findTrialsEndingBetween(start, end));
    }
}
