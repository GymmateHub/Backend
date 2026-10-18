package com.gymmate.ai.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.ai.internal.domain.AiRecommendation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiRecommendationRepository extends DomainRepository<AiRecommendation, UUID> {

    /** Returns the most recently generated plan for a given member. */
    Optional<AiRecommendation> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId);

    /** Returns full plan history for a member (newest first). */
    List<AiRecommendation> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
