package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.application.port.NewsletterCampaignRepository;
import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link NewsletterCampaignRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the NewsletterCampaign finders live here.
 */
@Component
@Transactional()
public class NewsletterCampaignRepositoryAdapter extends JpaDomainRepositoryAdapter<NewsletterCampaign, UUID, NewsletterCampaignJpaRepository>
        implements NewsletterCampaignRepository {

    public NewsletterCampaignRepositoryAdapter(NewsletterCampaignJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public List<NewsletterCampaign> findByGymIdOrderByCreatedAtDesc(UUID gymId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId));
    }

    @Override
    public List<NewsletterCampaign> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId) {
        return this.<List<NewsletterCampaign>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId));
    }
}
