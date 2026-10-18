package com.gymmate.notification.internal.infrastructure.persistence;

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
public interface NewsletterTemplateJpaRepository extends JpaRepository<NewsletterTemplateJpaEntity, UUID> {

    List<NewsletterTemplateJpaEntity> findByGymId(UUID gymId);

    @Query("SELECT t FROM NewsletterTemplate t WHERE t.gymId = :gymId AND t.active = true")
    List<NewsletterTemplateJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    List<NewsletterTemplateJpaEntity> findByOrganisationId(UUID organisationId);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
