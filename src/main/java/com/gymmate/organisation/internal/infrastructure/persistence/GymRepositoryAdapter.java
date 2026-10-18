package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.organisation.internal.application.port.GymRepository;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.constants.GymStatus;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Gym finders live here.
 */
@Component
@Transactional()
public class GymRepositoryAdapter extends JpaDomainRepositoryAdapter<Gym, UUID, GymJpaRepository>
        implements GymRepository {

    public GymRepositoryAdapter(GymJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    // ========== Organisation-based queries ==========
    @Override
    public List<Gym> findByOrganisationId(UUID organisationId) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Gym> findByOrganisationIdAndStatus(UUID organisationId, GymStatus status) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public Optional<Gym> findBySlug(String slug) {
        return this.<Optional<Gym>>fromJpa(jpaRepository.findBySlug(slug));
    }

    // ========== General queries ==========
    @Override
    public List<Gym> findByStatus(GymStatus status) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByStatus(status));
    }

    @Override
    public List<Gym> findByAddressCity(String city) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByCity(city));
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, GymStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public Integer sumMaxMembersByOrganisationId(UUID organisationId) {
        return jpaRepository.sumMaxMembersByOrganisationId(organisationId);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public List<Gym> findByCity(String city) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByCity(city));
    }
}
