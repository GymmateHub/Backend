package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.application.port.NewsletterTemplateRepository;
import com.gymmate.notification.internal.domain.NewsletterTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link NewsletterTemplateRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class NewsletterTemplateRepositoryAdapter extends DomainRepositoryAdapter implements NewsletterTemplateRepository {

    private final NewsletterTemplateJpaRepository jpaRepository;

    public NewsletterTemplateRepositoryAdapter(NewsletterTemplateJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NewsletterTemplate save(NewsletterTemplate template) {
        return save(jpaRepository, template);
    }

    @Override
    public Optional<NewsletterTemplate> findById(UUID id) {
        return this.<Optional<NewsletterTemplate>>fromJpa(jpaRepository.findById(id));
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
    public void delete(NewsletterTemplate template) {
        delete(jpaRepository, template);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }

    @Override
    public List<NewsletterTemplate> saveAll(Iterable<NewsletterTemplate> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<NewsletterTemplate> findAll() {
        return this.<List<NewsletterTemplate>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<NewsletterTemplate> findAllById(Iterable<UUID> ids) {
        return this.<List<NewsletterTemplate>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<NewsletterTemplate> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public NewsletterTemplate saveAndFlush(NewsletterTemplate entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<NewsletterTemplate> findAll(Pageable pageable) {
        return this.<Page<NewsletterTemplate>>fromJpa(jpaRepository.findAll(pageable));
    }
}
