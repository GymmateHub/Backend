package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.NewsletterTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter implementing the domain repository interface.
 */
@Component
@RequiredArgsConstructor
public class NewsletterTemplateRepositoryAdapter implements NewsletterTemplateRepository {

    private final NewsletterTemplateJpaRepository jpaRepository;

    @Override
    public NewsletterTemplate save(NewsletterTemplate template) {
        return jpaRepository.save(template);
    }

    @Override
    public Optional<NewsletterTemplate> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<NewsletterTemplate> findByGymId(UUID gymId) {
        return jpaRepository.findByGymId(gymId);
    }

    @Override
    public List<NewsletterTemplate> findActiveByGymId(UUID gymId) {
        return jpaRepository.findActiveByGymId(gymId);
    }

    @Override
    public List<NewsletterTemplate> findByOrganisationId(UUID organisationId) {
        return jpaRepository.findByOrganisationId(organisationId);
    }

    @Override
    public void delete(NewsletterTemplate template) {
        jpaRepository.delete(template);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
