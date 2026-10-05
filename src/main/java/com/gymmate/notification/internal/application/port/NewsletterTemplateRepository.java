package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.NewsletterTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository interface for NewsletterTemplate domain entity.
 */
public interface NewsletterTemplateRepository {

    NewsletterTemplate save(NewsletterTemplate template);

    Optional<NewsletterTemplate> findById(UUID id);

    List<NewsletterTemplate> findByGymId(UUID gymId);

    List<NewsletterTemplate> findActiveByGymId(UUID gymId);

    List<NewsletterTemplate> findByOrganisationId(UUID organisationId);

    void delete(NewsletterTemplate template);

    boolean existsByGymIdAndName(UUID gymId, String name);
    
    List<NewsletterTemplate> saveAll(Iterable<NewsletterTemplate> entities);
    
    boolean existsById(UUID id);
    
    List<NewsletterTemplate> findAll();
    
    List<NewsletterTemplate> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void deleteAll(Iterable<NewsletterTemplate> entities);
    
    NewsletterTemplate saveAndFlush(NewsletterTemplate entity);
    
    void flush();
    
    Page<NewsletterTemplate> findAll(Pageable pageable);
}
