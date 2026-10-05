package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.CampaignRecipient;
import com.gymmate.notification.internal.domain.RecipientStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CampaignRecipient domain entity.
 */
public interface CampaignRecipientRepository {

    CampaignRecipient save(CampaignRecipient recipient);

    List<CampaignRecipient> saveAll(List<CampaignRecipient> recipients);

    Optional<CampaignRecipient> findById(UUID id);

    List<CampaignRecipient> findByCampaignId(UUID campaignId);

    List<CampaignRecipient> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    int countByCampaignId(UUID campaignId);

    int countByCampaignIdAndStatus(UUID campaignId, RecipientStatus status);

    void deleteByCampaignId(UUID campaignId);
}
