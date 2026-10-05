package com.gymmate.shared.infrastructure.persistence;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Identity map between domain objects and the JPA entities managed by one Hibernate session —
 * the domain-side mirror of the persistence context.
 *
 * <ul>
 *   <li>Loading the same row twice yields the same domain instance (as JPA does for entities).</li>
 *   <li>Changes made to loaded domain objects are copied onto their managed entities before every
 *       flush ({@link #syncToEntities()}), so "load, modify, commit" keeps working without an
 *       explicit save — exactly the dirty-checking contract application services relied on.</li>
 *   <li>Database-generated state (ids, audit timestamps, tenant ids set on insert) is copied back
 *       onto the domain objects after inserts/updates ({@link #refreshDomain(Object)}).</li>
 * </ul>
 *
 * Instances are bound to the current transaction by {@link DomainPersistenceContexts}.
 */
public final class MappingContext {

    private final PersistenceMappers mappers;
    private final SessionImplementor session;

    private final Map<Object, Object> domainByEntity = new IdentityHashMap<>();
    private final Map<Object, Object> entityByDomain = new IdentityHashMap<>();

    /** Guards against re-copying the same object while walking a cyclic graph in one operation. */
    private Set<Object> visiting;

    MappingContext(PersistenceMappers mappers, SessionImplementor session) {
        this.mappers = mappers;
        this.session = session;
    }

    // ------------------------------------------------------------------ entity -> domain

    /** Domain object for a (possibly proxied) entity; creates and binds it on first sight. */
    Object domainFor(Object entityOrProxy) {
        if (entityOrProxy == null) {
            return null;
        }
        Object entity = Hibernate.unproxy(entityOrProxy);
        Object domain = domainByEntity.get(entity);
        if (domain != null) {
            return domain;
        }
        EntityMapping<?, ?> mapping = requireEntityMapping(entity);
        domain = mapping.newDomain();
        bind(domain, entity);
        boolean outermost = enter();
        try {
            visiting.add(domain);
            mapping.copyToDomain(entity, domain, this);
        } finally {
            exit(outermost);
        }
        return domain;
    }

    // ------------------------------------------------------------------ domain -> entity

    /**
     * Managed (or new, transient) entity carrying the domain object's current state.
     * Untracked domain objects that have an id are attached to the managed row (merge semantics).
     */
    Object entityFor(Object domain) {
        if (domain == null) {
            return null;
        }
        EntityMapping<?, ?> mapping = mappers.forDomain(domain.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(domain.getClass().getName() + " is not a mapped domain type");
        }
        Object entity = entityByDomain.get(domain);
        if (entity == null) {
            Object id = mapping.domainId(domain);
            if (id != null) {
                entity = session.find(mapping.entityType(), id);
            }
            if (entity == null) {
                entity = mapping.newEntity();
            }
            bind(domain, entity);
        }
        boolean outermost = enter();
        try {
            if (visiting.add(domain)) {
                mapping.copyToEntity(domain, entity, this);
            }
        } finally {
            exit(outermost);
        }
        return entity;
    }

    // ------------------------------------------------------------------ lifecycle

    /** Copies every tracked domain object onto its entity; called before each flush. */
    void syncToEntities() {
        if (entityByDomain.isEmpty()) {
            return;
        }
        boolean outermost = enter();
        try {
            // entityFor may bind new (cascaded) objects; iterate over a snapshot until stable
            List<Object> pending = new ArrayList<>(entityByDomain.keySet());
            while (!pending.isEmpty()) {
                for (Object domain : pending) {
                    entityFor(domain);
                }
                pending = new ArrayList<>();
                for (Object domain : entityByDomain.keySet()) {
                    if (!visiting.contains(domain)) {
                        pending.add(domain);
                    }
                }
            }
        } finally {
            exit(outermost);
        }
    }

    /** Copies database-assigned values of a just-written entity back onto its domain object. */
    void refreshDomain(Object entity) {
        Object domain = domainByEntity.get(entity);
        if (domain != null) {
            requireEntityMapping(entity).copyValuesToDomain(entity, domain);
        }
    }

    /** Full entity → domain copy for a tracked pair (after save, so cascaded children get ids). */
    void refreshDomainGraph(Object domain, Object entity) {
        boolean outermost = enter();
        try {
            visiting.add(domain);
            requireEntityMapping(entity).copyToDomain(entity, domain, this);
        } finally {
            exit(outermost);
        }
    }

    /** Re-binds the domain object when the repository returned a different (merged) instance. */
    void bindSaved(Object domain, Object savedEntity) {
        if (entityByDomain.get(domain) != savedEntity) {
            bind(domain, savedEntity);
        }
    }

    void forget(Object domain) {
        Object entity = entityByDomain.remove(domain);
        if (entity != null) {
            domainByEntity.remove(entity);
        }
    }

    void clear() {
        domainByEntity.clear();
        entityByDomain.clear();
    }

    boolean isTracked(Object domain) {
        return entityByDomain.containsKey(domain);
    }

    PersistenceMappers mappers() {
        return mappers;
    }

    // ------------------------------------------------------------------ internals

    private void bind(Object domain, Object entity) {
        // A row is represented by exactly one domain object: the latest one bound wins
        // (merge semantics) so a stale copy can never overwrite newer state at flush.
        Object previous = domainByEntity.put(entity, domain);
        if (previous != null && previous != domain) {
            entityByDomain.remove(previous);
        }
        entityByDomain.put(domain, entity);
    }

    private EntityMapping<?, ?> requireEntityMapping(Object entity) {
        EntityMapping<?, ?> mapping = mappers.forEntity(entity);
        if (mapping == null) {
            throw new IllegalArgumentException(entity.getClass().getName() + " is not a mapped entity type");
        }
        return mapping;
    }

    private boolean enter() {
        if (visiting == null) {
            visiting = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            return true;
        }
        return false;
    }

    private void exit(boolean outermost) {
        if (outermost) {
            visiting = null;
        }
    }
}
