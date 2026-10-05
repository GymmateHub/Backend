package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.AccessCredential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.AccessCredentialRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessCredentialRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AccessCredentialRepositoryAdapter extends DomainRepositoryAdapter implements AccessCredentialRepository {

    private final AccessCredentialJpaRepository jpaRepository;

    public AccessCredentialRepositoryAdapter(AccessCredentialJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AccessCredential> findByTokenHashAndActiveTrue(String tokenHash) {
        return this.<Optional<AccessCredential>>fromJpa(jpaRepository.findByTokenHashAndActiveTrue(tokenHash));
    }

    @Override
    public List<AccessCredential> findByMemberId(UUID memberId) {
        return this.<List<AccessCredential>>fromJpa(jpaRepository.findByMemberId(memberId));
    }

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return jpaRepository.existsByTokenHash(tokenHash);
    }

    @Override
    public AccessCredential save(AccessCredential entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AccessCredential> saveAll(Iterable<AccessCredential> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AccessCredential> findById(UUID id) {
        return this.<Optional<AccessCredential>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AccessCredential> findAll() {
        return this.<List<AccessCredential>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AccessCredential> findAllById(Iterable<UUID> ids) {
        return this.<List<AccessCredential>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(AccessCredential entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AccessCredential> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AccessCredential saveAndFlush(AccessCredential entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AccessCredential> findAll(Pageable pageable) {
        return this.<Page<AccessCredential>>fromJpa(jpaRepository.findAll(pageable));
    }
}
