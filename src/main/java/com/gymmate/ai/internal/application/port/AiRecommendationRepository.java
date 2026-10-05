package com.gymmate.ai.internal.application.port;

import com.gymmate.ai.internal.domain.AiRecommendation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AiRecommendationRepository {

    /** Returns the most recently generated plan for a given member. */
    Optional<AiRecommendation> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId);

    /** Returns full plan history for a member (newest first). */
    List<AiRecommendation> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
    
    AiRecommendation save(AiRecommendation entity);
    
    List<AiRecommendation> saveAll(Iterable<AiRecommendation> entities);
    
    Optional<AiRecommendation> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<AiRecommendation> findAll();
    
    List<AiRecommendation> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(AiRecommendation entity);
    
    void deleteAll(Iterable<AiRecommendation> entities);
    
    AiRecommendation saveAndFlush(AiRecommendation entity);
    
    void flush();
    
    Page<AiRecommendation> findAll(Pageable pageable);
}
