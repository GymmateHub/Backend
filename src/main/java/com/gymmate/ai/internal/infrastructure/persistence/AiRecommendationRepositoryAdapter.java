package com.gymmate.ai.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.ai.internal.domain.AiRecommendation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.ai.internal.application.port.AiRecommendationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AiRecommendationRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AiRecommendation finders live here.
 */
@Component()
@Transactional()
public class AiRecommendationRepositoryAdapter extends JpaDomainRepositoryAdapter<AiRecommendation, UUID, AiRecommendationJpaRepository>
        implements AiRecommendationRepository {

    public AiRecommendationRepositoryAdapter(AiRecommendationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<AiRecommendation> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<Optional<AiRecommendation>>fromJpa(jpaRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public List<AiRecommendation> findByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<List<AiRecommendation>>fromJpa(jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId));
    }
}
