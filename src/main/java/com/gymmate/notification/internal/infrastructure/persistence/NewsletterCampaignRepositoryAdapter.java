package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.application.port.NewsletterCampaignRepository;
import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
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
 * Persistence adapter implementing {@link NewsletterCampaignRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class NewsletterCampaignRepositoryAdapter extends DomainRepositoryAdapter implements NewsletterCampaignRepository {

    private final NewsletterCampaignJpaRepository jpaRepository;

    public NewsletterCampaignRepositoryAdapter(NewsletterCampaignJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NewsletterCampaign save(NewsletterCampaign campaign) {
        return save(jpaRepository, campaign);
    }

    @Override
    public Optional<NewsletterCampaign> findById(UUID id) {
        return this.<Optional<NewsletterCampaign>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<NewsletterCampaign> findByGymId(UUID gymId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId));
    }

    @Override
    public List<NewsletterCampaign> findByGymIdAndStatus(UUID gymId, CampaignStatus status) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<NewsletterCampaign> findScheduledCampaignsReadyToSend(LocalDateTime now) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findScheduledCampaignsReadyToSend(now));
    }

    @Override
    public List<NewsletterCampaign> findByOrganisationId(UUID organisationId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId));
    }

    @Override
    public void delete(NewsletterCampaign campaign) {
        delete(jpaRepository, campaign);
    }

    @Override
    public List<NewsletterCampaign> findByGymIdOrderByCreatedAtDesc(UUID gymId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId));
    }

    @Override
    public List<NewsletterCampaign> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId));
    }

    @Override
    public List<NewsletterCampaign> saveAll(Iterable<NewsletterCampaign> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<NewsletterCampaign> findAll() {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<NewsletterCampaign> findAllById(Iterable<UUID> ids) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<NewsletterCampaign> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public NewsletterCampaign saveAndFlush(NewsletterCampaign entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<NewsletterCampaign> findAll(Pageable pageable) {
        return this.<Page<NewsletterCampaign>>fromJpa(jpaRepository.findAll(pageable));
    }
}
