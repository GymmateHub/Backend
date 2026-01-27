package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.CampaignStatus;
import com.gymmate.notification.domain.NewsletterCampaign;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NewsletterCampaign domain entity.
 */
public interface NewsletterCampaignRepository {

    NewsletterCampaign save(NewsletterCampaign campaign);

    Optional<NewsletterCampaign> findById(UUID id);

    List<NewsletterCampaign> findByGymId(UUID gymId);

    List<NewsletterCampaign> findByGymIdAndStatus(UUID gymId, CampaignStatus status);

    List<NewsletterCampaign> findScheduledCampaignsReadyToSend(LocalDateTime now);

    List<NewsletterCampaign> findByOrganisationId(UUID organisationId);

    void delete(NewsletterCampaign campaign);
}
