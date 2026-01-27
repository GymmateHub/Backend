package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.CampaignRecipient;
import com.gymmate.notification.domain.RecipientStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter implementing the domain repository interface for
 * recipients.
 */
@Component
@RequiredArgsConstructor
public class CampaignRecipientRepositoryAdapter implements CampaignRecipientRepository {

    private final CampaignRecipientJpaRepository jpaRepository;

    @Override
    public CampaignRecipient save(CampaignRecipient recipient) {
        return jpaRepository.save(recipient);
    }

    @Override
    public List<CampaignRecipient> saveAll(List<CampaignRecipient> recipients) {
        return jpaRepository.saveAll(recipients);
    }

    @Override
    public Optional<CampaignRecipient> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CampaignRecipient> findByCampaignId(UUID campaignId) {
        return jpaRepository.findByCampaignId(campaignId);
    }

    @Override
    public List<CampaignRecipient> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status) {
        return jpaRepository.findByCampaignIdAndStatus(campaignId, status);
    }

    @Override
    public int countByCampaignId(UUID campaignId) {
        return jpaRepository.countByCampaignId(campaignId);
    }

    @Override
    public int countByCampaignIdAndStatus(UUID campaignId, RecipientStatus status) {
        return jpaRepository.countByCampaignIdAndStatus(campaignId, status);
    }

    @Override
    public void deleteByCampaignId(UUID campaignId) {
        jpaRepository.deleteByCampaignId(campaignId);
    }
}
