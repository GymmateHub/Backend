package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.application.port.CampaignRecipientRepository;
import com.gymmate.notification.internal.domain.CampaignRecipient;
import com.gymmate.notification.internal.domain.RecipientStatus;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link CampaignRecipientRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class CampaignRecipientRepositoryAdapter extends DomainRepositoryAdapter implements CampaignRecipientRepository {

    private final CampaignRecipientJpaRepository jpaRepository;

    public CampaignRecipientRepositoryAdapter(CampaignRecipientJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CampaignRecipient save(CampaignRecipient recipient) {
        return save(jpaRepository, recipient);
    }

    @Override
    public List<CampaignRecipient> saveAll(List<CampaignRecipient> recipients) {
        return saveAll(jpaRepository, recipients);
    }

    @Override
    public Optional<CampaignRecipient> findById(UUID id) {
        return this.<Optional<CampaignRecipient>>fromJpa(jpaRepository.findById(id));
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

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<CampaignRecipient> findAll() {
        return this.<List<CampaignRecipient>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<CampaignRecipient> findAllById(Iterable<UUID> ids) {
        return this.<List<CampaignRecipient>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(CampaignRecipient entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<CampaignRecipient> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public CampaignRecipient saveAndFlush(CampaignRecipient entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<CampaignRecipient> findAll(Pageable pageable) {
        return this.<Page<CampaignRecipient>>fromJpa(jpaRepository.findAll(pageable));
    }
}
