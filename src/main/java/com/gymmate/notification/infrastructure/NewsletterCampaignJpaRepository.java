package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.CampaignStatus;
import com.gymmate.notification.domain.NewsletterCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for NewsletterCampaign.
 */
@Repository
public interface NewsletterCampaignJpaRepository extends JpaRepository<NewsletterCampaign, UUID> {

    List<NewsletterCampaign> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    List<NewsletterCampaign> findByGymIdAndStatus(UUID gymId, CampaignStatus status);

    @Query("SELECT c FROM NewsletterCampaign c WHERE c.status = 'SCHEDULED' AND c.scheduledAt <= :now")
    List<NewsletterCampaign> findScheduledCampaignsReadyToSend(@Param("now") LocalDateTime now);

    List<NewsletterCampaign> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);
}
