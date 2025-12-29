package com.gymmate.subscription.infrastructure;

import com.gymmate.subscription.domain.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionTierRepository extends JpaRepository<SubscriptionTier, UUID> {

    Optional<SubscriptionTier> findByName(String name);

    List<SubscriptionTier> findByActiveTrueOrderBySortOrder();

    @Query("SELECT st FROM SubscriptionTier st WHERE st.active = true AND st.featured = true ORDER BY st.sortOrder")
    List<SubscriptionTier> findFeaturedTiers();

    @Query("SELECT st FROM SubscriptionTier st WHERE st.maxMembers >= :memberCount ORDER BY st.price ASC")
    List<SubscriptionTier> findSuitableTiersForMemberCount(@Param("memberCount") Integer memberCount);
}

