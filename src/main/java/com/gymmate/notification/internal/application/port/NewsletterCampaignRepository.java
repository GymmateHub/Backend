package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    
    List<NewsletterCampaign> findByGymIdOrderByCreatedAtDesc(UUID gymId);
    
    List<NewsletterCampaign> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);
    
    List<NewsletterCampaign> saveAll(Iterable<NewsletterCampaign> entities);
    
    boolean existsById(UUID id);
    
    List<NewsletterCampaign> findAll();
    
    List<NewsletterCampaign> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void deleteAll(Iterable<NewsletterCampaign> entities);
    
    NewsletterCampaign saveAndFlush(NewsletterCampaign entity);
    
    void flush();
    
    Page<NewsletterCampaign> findAll(Pageable pageable);
}
