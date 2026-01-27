package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.CampaignRecipient;
import com.gymmate.notification.domain.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CampaignRecipient.
 */
@Repository
public interface CampaignRecipientJpaRepository extends JpaRepository<CampaignRecipient, UUID> {

    List<CampaignRecipient> findByCampaignId(UUID campaignId);

    List<CampaignRecipient> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    int countByCampaignId(UUID campaignId);

    int countByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    void deleteByCampaignId(UUID campaignId);
}
