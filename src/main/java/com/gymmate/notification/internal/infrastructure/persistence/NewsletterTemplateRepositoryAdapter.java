package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.application.port.NewsletterTemplateRepository;
import com.gymmate.notification.internal.domain.NewsletterTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link NewsletterTemplateRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the NewsletterTemplate finders live here.
 */
@Component
@Transactional()
public class NewsletterTemplateRepositoryAdapter extends JpaDomainRepositoryAdapter<NewsletterTemplate, UUID, NewsletterTemplateJpaRepository>
        implements NewsletterTemplateRepository {

    public NewsletterTemplateRepositoryAdapter(NewsletterTemplateJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<NewsletterTemplate> findByGymId(UUID gymId) {
        return this.<List<NewsletterTemplate>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<NewsletterTemplate> findActiveByGymId(UUID gymId) {
        return this.<List<NewsletterTemplate>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public List<NewsletterTemplate> findByOrganisationId(UUID organisationId) {
        return this.<List<NewsletterTemplate>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
