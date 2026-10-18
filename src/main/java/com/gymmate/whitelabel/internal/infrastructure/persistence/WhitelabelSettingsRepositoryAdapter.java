package com.gymmate.whitelabel.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.whitelabel.internal.application.port.WhitelabelSettingsRepository;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WhitelabelSettingsRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the WhitelabelSettings finders live here.
 */
@Component
@Transactional()
public class WhitelabelSettingsRepositoryAdapter extends JpaDomainRepositoryAdapter<WhitelabelSettings, UUID, WhitelabelSettingsJpaRepository>
        implements WhitelabelSettingsRepository {

    public WhitelabelSettingsRepositoryAdapter(WhitelabelSettingsJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByOrganisationIdAndGymIdIsNull(organisationId));
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId));
    }

    @Override
    public Optional<WhitelabelSettings> findByGymId(UUID gymId) {
        return this.<Optional<WhitelabelSettings>>fromJpa(jpaRepository.findByGymId(gymId));
    }
}
