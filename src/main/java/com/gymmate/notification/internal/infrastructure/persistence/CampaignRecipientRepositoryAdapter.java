package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.application.port.CampaignRecipientRepository;
import com.gymmate.notification.internal.domain.CampaignRecipient;
import com.gymmate.notification.internal.domain.RecipientStatus;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link CampaignRecipientRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the CampaignRecipient finders live here.
 */
@Component
@Transactional()
public class CampaignRecipientRepositoryAdapter extends JpaDomainRepositoryAdapter<CampaignRecipient, UUID, CampaignRecipientJpaRepository>
        implements CampaignRecipientRepository {

    public CampaignRecipientRepositoryAdapter(CampaignRecipientJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<CampaignRecipient> findByCampaignId(UUID campaignId) {
        return this.<List<CampaignRecipient>>fromJpa(jpaRepository.findByCampaignId(campaignId));
    }

    @Override
    public List<CampaignRecipient> findByCampaignIdAndStatus(UUID campaignId, RecipientStatus status) {
        return this.<List<CampaignRecipient>>fromJpa(jpaRepository.findByCampaignIdAndStatus(campaignId, status));
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
