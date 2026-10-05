package com.gymmate.ai.internal.infrastructure.persistence;

import com.gymmate.ai.internal.domain.AiRecommendation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.ai.internal.application.port.AiRecommendationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AiRecommendationRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AiRecommendationRepositoryAdapter extends DomainRepositoryAdapter implements AiRecommendationRepository {

    private final AiRecommendationJpaRepository jpaRepository;

    public AiRecommendationRepositoryAdapter(AiRecommendationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AiRecommendation> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<Optional<AiRecommendation>>fromJpa(jpaRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public List<AiRecommendation> findByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<List<AiRecommendation>>fromJpa(jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public AiRecommendation save(AiRecommendation entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AiRecommendation> saveAll(Iterable<AiRecommendation> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AiRecommendation> findById(UUID id) {
        return this.<Optional<AiRecommendation>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AiRecommendation> findAll() {
        return this.<List<AiRecommendation>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AiRecommendation> findAllById(Iterable<UUID> ids) {
        return this.<List<AiRecommendation>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(AiRecommendation entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AiRecommendation> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AiRecommendation saveAndFlush(AiRecommendation entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AiRecommendation> findAll(Pageable pageable) {
        return this.<Page<AiRecommendation>>fromJpa(jpaRepository.findAll(pageable));
    }
}
