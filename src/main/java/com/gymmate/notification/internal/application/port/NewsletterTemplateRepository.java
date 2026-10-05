package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.NewsletterTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NewsletterTemplate domain entity.
 */
public interface NewsletterTemplateRepository {

    NewsletterTemplate save(NewsletterTemplate template);

    Optional<NewsletterTemplate> findById(UUID id);

    List<NewsletterTemplate> findByGymId(UUID gymId);

    List<NewsletterTemplate> findActiveByGymId(UUID gymId);

    List<NewsletterTemplate> findByOrganisationId(UUID organisationId);

    void delete(NewsletterTemplate template);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
