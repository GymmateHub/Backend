package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.AccessCredential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.access.internal.application.port.AccessCredentialRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessCredentialRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AccessCredential finders live here.
 */
@Component()
@Transactional()
public class AccessCredentialRepositoryAdapter extends JpaDomainRepositoryAdapter<AccessCredential, UUID, AccessCredentialJpaRepository>
        implements AccessCredentialRepository {

    public AccessCredentialRepositoryAdapter(AccessCredentialJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
