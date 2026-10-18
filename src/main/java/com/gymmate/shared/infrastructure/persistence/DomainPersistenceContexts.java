package com.gymmate.shared.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Binds one {@link MappingContext} to each Hibernate session participating in a Spring
 * transaction, and exposes it to the Hibernate event listeners registered by
 * {@link DomainPersistenceHibernateConfig}.
 *
 * <p>The context is stored as a transaction-scoped resource keyed by the session, so nested
 * {@code REQUIRES_NEW} transactions (separate sessions) get separate identity maps, and the
 * context is released when the transaction completes — after which domain objects behave like
 * detached entities.
 */
@Component
public class DomainPersistenceContexts {

    private final PersistenceMappers mappers;
    private final EntityManagerFactory entityManagerFactory;

    public DomainPersistenceContexts(PersistenceMappers mappers, EntityManagerFactory entityManagerFactory) {
        this.mappers = mappers;
        this.entityManagerFactory = entityManagerFactory;
    }

    /** The context of the session bound to the current transaction (requires an active transaction). */
    public MappingContext current() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Domain repositories must be called within a transaction (adapters are @Transactional)");
        }
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (em == null) {
            throw new IllegalStateException("No transactional EntityManager bound to the current thread");
        }
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        MappingContext ctx = lookup(session);
        if (ctx == null) {
            ctx = new MappingContext(mappers, session);
            Key key = new Key(session);
            TransactionSynchronizationManager.bindResource(key, ctx);
            // Released with the transaction; the key is per session, so contexts of suspended
            // outer transactions (REQUIRES_NEW) stay bound and never collide with inner ones.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (TransactionSynchronizationManager.hasResource(key)) {
                        TransactionSynchronizationManager.unbindResource(key);
                    }
                }
            });
        }
        return ctx;
    }

    /** The context bound for this session on the current thread, or null (used by Hibernate listeners). */
    static MappingContext lookup(SharedSessionContractImplementor session) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return null;
        }
        return (MappingContext) TransactionSynchronizationManager.getResource(new Key(session));
    }

    /** Identity-based resource key for a session. */
    private record Key(SharedSessionContractImplementor session) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && k.session == session;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(session);
        }
    }
}
