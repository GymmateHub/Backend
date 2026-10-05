package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.domain.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CampaignRecipient.
 */
@Repository
public interface CampaignRecipientJpaRepository extends JpaRepository<CampaignRecipientJpaEntity, UUID> {

    List<CampaignRecipientJpaEntity> findByCampaignId(UUID campaignId);

    List<CampaignRecipientJpaEntity> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    int countByCampaignId(UUID campaignId);

    int countByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    void deleteByCampaignId(UUID campaignId);
}
