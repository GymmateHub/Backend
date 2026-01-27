package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.CampaignStatus;
import com.gymmate.notification.domain.NewsletterCampaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter implementing the domain repository interface for
 * campaigns.
 */
@Component
@RequiredArgsConstructor
public class NewsletterCampaignRepositoryAdapter implements NewsletterCampaignRepository {

    private final NewsletterCampaignJpaRepository jpaRepository;

    @Override
    public NewsletterCampaign save(NewsletterCampaign campaign) {
        return jpaRepository.save(campaign);
    }

    @Override
    public Optional<NewsletterCampaign> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<NewsletterCampaign> findByGymId(UUID gymId) {
        return jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId);
    }

    @Override
    public List<NewsletterCampaign> findByGymIdAndStatus(UUID gymId, CampaignStatus status) {
        return jpaRepository.findByGymIdAndStatus(gymId, status);
    }

    @Override
    public List<NewsletterCampaign> findScheduledCampaignsReadyToSend(LocalDateTime now) {
        return jpaRepository.findScheduledCampaignsReadyToSend(now);
    }

    @Override
    public List<NewsletterCampaign> findByOrganisationId(UUID organisationId) {
        return jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId);
    }

    @Override
    public void delete(NewsletterCampaign campaign) {
        jpaRepository.delete(campaign);
    }
}
