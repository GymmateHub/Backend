package com.gymmate.notification.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for NewsletterCampaign domain entity.
 */
public interface NewsletterCampaignRepository extends DomainRepository<NewsletterCampaign, UUID> {

    List<NewsletterCampaign> findByGymId(UUID gymId);

    List<NewsletterCampaign> findByGymIdAndStatus(UUID gymId, CampaignStatus status);

    List<NewsletterCampaign> findScheduledCampaignsReadyToSend(LocalDateTime now);

    List<NewsletterCampaign> findByOrganisationId(UUID organisationId);

    List<NewsletterCampaign> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    List<NewsletterCampaign> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);
}
