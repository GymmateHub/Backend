package com.gymmate.ai.infrastructure;

import com.gymmate.ai.domain.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {

    /** Returns the most recently generated plan for a given member. */
    Optional<AiRecommendation> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId);

    /** Returns full plan history for a member (newest first). */
    List<AiRecommendation> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
