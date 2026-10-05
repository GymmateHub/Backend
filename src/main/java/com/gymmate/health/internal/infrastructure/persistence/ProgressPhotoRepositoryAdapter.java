package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.ProgressPhotoRepository;
import com.gymmate.health.internal.domain.ProgressPhoto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ProgressPhotoRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class ProgressPhotoRepositoryAdapter extends DomainRepositoryAdapter implements ProgressPhotoRepository {

    private final ProgressPhotoJpaRepository jpaRepository;

    public ProgressPhotoRepositoryAdapter(ProgressPhotoJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProgressPhoto save(ProgressPhoto progressPhoto) {
        return save(jpaRepository, progressPhoto);
    }

    @Override
    public Optional<ProgressPhoto> findById(UUID id) {
        return this.<Optional<ProgressPhoto>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<ProgressPhoto> findByMemberId(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }

    @Override
    public List<ProgressPhoto> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate));
    }

    @Override
    public List<ProgressPhoto> findPublicPhotosByMemberId(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findPublicPhotosByMemberId(memberId));
    }

    @Override
    public List<ProgressPhoto> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public Optional<ProgressPhoto> findLatestByMemberId(UUID memberId) {
        return this.<Optional<ProgressPhoto>>fromJpa(jpaRepository.findLatestByMemberId(memberId));
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void delete(ProgressPhoto progressPhoto) {
        progressPhoto.setActive(false);
        save(jpaRepository, progressPhoto);
    }

    @Override
    public List<ProgressPhoto> findByMemberIdOrderByDateDesc(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }

    @Override
    public List<ProgressPhoto> saveAll(Iterable<ProgressPhoto> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ProgressPhoto> findAll() {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ProgressPhoto> findAllById(Iterable<UUID> ids) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<ProgressPhoto> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ProgressPhoto saveAndFlush(ProgressPhoto entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ProgressPhoto> findAll(Pageable pageable) {
        return this.<Page<ProgressPhoto>>fromJpa(jpaRepository.findAll(pageable));
    }
}
