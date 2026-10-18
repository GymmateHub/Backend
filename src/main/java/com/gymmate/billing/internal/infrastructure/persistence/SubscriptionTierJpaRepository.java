package com.gymmate.billing.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionTierJpaRepository extends JpaRepository<SubscriptionTierJpaEntity, UUID> {

    Optional<SubscriptionTierJpaEntity> findByName(String name);

    List<SubscriptionTierJpaEntity> findByActiveTrueOrderBySortOrder();

    @Query("SELECT st FROM SubscriptionTier st WHERE st.active = true AND st.featured = true ORDER BY st.sortOrder")
    List<SubscriptionTierJpaEntity> findFeaturedTiers();

    @Query("SELECT st FROM SubscriptionTier st WHERE st.maxMembers >= :memberCount ORDER BY st.price ASC")
    List<SubscriptionTierJpaEntity> findSuitableTiersForMemberCount(@Param("memberCount") Integer memberCount);
}
