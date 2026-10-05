package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.domain.CampaignStatus;
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
public interface NewsletterCampaignJpaRepository extends JpaRepository<NewsletterCampaignJpaEntity, UUID> {

    List<NewsletterCampaignJpaEntity> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    List<NewsletterCampaignJpaEntity> findByGymIdAndStatus(UUID gymId, CampaignStatus status);

    @Query("SELECT c FROM NewsletterCampaign c WHERE c.status = 'SCHEDULED' AND c.scheduledAt <= :now")
    List<NewsletterCampaignJpaEntity> findScheduledCampaignsReadyToSend(@Param("now") LocalDateTime now);

    List<NewsletterCampaignJpaEntity> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);
}
