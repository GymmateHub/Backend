package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.NewsletterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for NewsletterTemplate.
 */
@Repository
public interface NewsletterTemplateJpaRepository extends JpaRepository<NewsletterTemplate, UUID> {

    List<NewsletterTemplate> findByGymId(UUID gymId);

    @Query("SELECT t FROM NewsletterTemplate t WHERE t.gymId = :gymId AND t.active = true")
    List<NewsletterTemplate> findActiveByGymId(@Param("gymId") UUID gymId);

    List<NewsletterTemplate> findByOrganisationId(UUID organisationId);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
