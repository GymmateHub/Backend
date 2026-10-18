package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.Trainer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.TrainerRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link TrainerRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Trainer finders live here.
 */
@Component()
@Transactional()
public class TrainerRepositoryAdapter extends JpaDomainRepositoryAdapter<Trainer, UUID, TrainerJpaRepository>
        implements TrainerRepository {

    public TrainerRepositoryAdapter(TrainerJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<Trainer> findByUserId(UUID userId) {
        return this.<Optional<Trainer>>fromJpa(jpaRepository.findByUserId(userId));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public List<Trainer> findByOrganisationId(UUID organisationId) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Trainer> findActiveAndAcceptingClientsByOrganisationId(UUID organisationId) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findActiveAndAcceptingClientsByOrganisationId(organisationId));
    }

    @Override
    public List<Trainer> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByOrganisationIdAndEmploymentType(organisationId, employmentType));
    }

    @Override
    public List<Trainer> findActiveAndAcceptingClients() {
        return this.<List<Trainer>>fromJpa(jpaRepository.findActiveAndAcceptingClients());
    }

    @Override
    public List<Trainer> findByAcceptingClients(boolean accepting) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByAcceptingClients(accepting));
    }

    @Override
    public List<Trainer> findByEmploymentType(String employmentType) {
        return this.<List<Trainer>>fromJpa(jpaRepository.findByEmploymentType(employmentType));
    }
}
