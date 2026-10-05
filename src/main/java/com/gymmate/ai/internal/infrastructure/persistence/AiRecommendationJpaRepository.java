package com.gymmate.ai.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiRecommendationJpaRepository extends JpaRepository<AiRecommendationJpaEntity, UUID> {

    /**
     * Returns the most recently generated plan for a given member.
     */
    Optional<AiRecommendationJpaEntity> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId);

    /**
     * Returns full plan history for a member (newest first).
     */
    List<AiRecommendationJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
