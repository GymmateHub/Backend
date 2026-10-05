package com.gymmate.shared.infrastructure.persistence;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.ClearEvent;
import org.hibernate.event.spi.ClearEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Hooks the domain identity map ({@link MappingContext}) into Hibernate's lifecycle:
 * <ul>
 *   <li>before every flush (explicit, commit or auto-flush before a query) domain changes are
 *       copied onto the managed entities, so Hibernate's dirty checking sees them;</li>
 *   <li>after every insert/update, database-assigned values are copied back onto the domain
 *       objects;</li>
 *   <li>clearing the persistence context clears the identity map.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class DomainPersistenceHibernateConfig {

    @Bean
    HibernatePropertiesCustomizer domainPersistenceIntegrator() {
        return properties -> properties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> List.of(new DomainSyncIntegrator()));
    }

    static final class DomainSyncIntegrator implements Integrator {

        @Override
        public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                              SessionFactoryImplementor sessionFactory) {
            EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                    .getService(EventListenerRegistry.class);
            DomainSyncListener listener = new DomainSyncListener();
            registry.prependListeners(EventType.FLUSH, listener);
            registry.prependListeners(EventType.AUTO_FLUSH, listener);
            registry.appendListeners(EventType.POST_INSERT, listener);
            registry.appendListeners(EventType.POST_UPDATE, listener);
            registry.appendListeners(EventType.CLEAR, listener);
        }

        @Override
        public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
            // nothing to release
        }
    }

    static final class DomainSyncListener implements FlushEventListener, AutoFlushEventListener,
            PostInsertEventListener, PostUpdateEventListener, ClearEventListener {

        @Override
        public void onFlush(FlushEvent event) {
            MappingContext ctx = DomainPersistenceContexts.lookup(event.getSession());
            if (ctx != null) {
                ctx.syncToEntities();
            }
        }

        @Override
        public void onAutoFlush(AutoFlushEvent event) {
            MappingContext ctx = DomainPersistenceContexts.lookup(event.getSession());
            if (ctx != null) {
                ctx.syncToEntities();
            }
        }

        @Override
        public void onPostInsert(PostInsertEvent event) {
            MappingContext ctx = DomainPersistenceContexts.lookup(event.getSession());
            if (ctx != null) {
                ctx.refreshDomain(event.getEntity());
            }
        }

        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            MappingContext ctx = DomainPersistenceContexts.lookup(event.getSession());
            if (ctx != null) {
                ctx.refreshDomain(event.getEntity());
            }
        }

        @Override
        public void onClear(ClearEvent event) {
            MappingContext ctx = DomainPersistenceContexts.lookup(event.getSession());
            if (ctx != null) {
                ctx.clear();
            }
        }

        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return false;
        }
    }
}
