package com.gymmate.organisation.infrastructure;

import com.gymmate.organisation.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Organisation entity.
 * Provides data access methods for organisation management.
 */
@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findBySlug(String slug);

    Optional<Organisation> findByOwnerUserId(UUID ownerUserId);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    @Query("SELECT o FROM Organisation o WHERE o.active = true")
    List<Organisation> findAllActive();

    @Query("SELECT o FROM Organisation o WHERE o.subscriptionStatus = :status")
    List<Organisation> findBySubscriptionStatus(@Param("status") String status);

    @Query("SELECT o FROM Organisation o WHERE o.trialEndsAt < :date AND o.subscriptionStatus = 'trial'")
    List<Organisation> findTrialsEndingBefore(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Organisation o WHERE o.subscriptionExpiresAt BETWEEN :start AND :end")
    List<Organisation> findSubscriptionsExpiringBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.active = true")
    long countActive();

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.subscriptionStatus = :status")
    long countBySubscriptionStatus(@Param("status") String status);

    @Query("SELECT o FROM Organisation o WHERE o.trialEndsAt BETWEEN :start AND :end AND o.subscriptionStatus = 'trial'")
    List<Organisation> findTrialsEndingBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

