package com.gymmate.notification.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.notification.internal.domain.NewsletterTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for NewsletterTemplate domain entity.
 */
public interface NewsletterTemplateRepository extends DomainRepository<NewsletterTemplate, UUID> {

    List<NewsletterTemplate> findByGymId(UUID gymId);

    List<NewsletterTemplate> findActiveByGymId(UUID gymId);

    List<NewsletterTemplate> findByOrganisationId(UUID organisationId);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
