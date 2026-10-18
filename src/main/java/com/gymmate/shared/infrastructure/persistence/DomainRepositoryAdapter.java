package com.gymmate.shared.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base class of the persistence adapters that implement application-layer repository ports on
 * top of Spring Data JPA repositories.
 *
 * <p>Converts arguments from domain objects to JPA entities ({@link #toJpa}) and results back
 * ({@link #fromJpa}) through the transaction's {@link MappingContext}, and implements the write
 * operations with JPA semantics: saved domain objects receive their generated id and audit/tenant
 * values; loaded domain objects stay attached for the rest of the transaction (later changes are
 * flushed without an explicit save). Subclasses must be {@code @Transactional} so that every port
 * call runs inside (or joins) a transaction.
 */
public abstract class DomainRepositoryAdapter {

    private final DomainPersistenceContexts contexts;

    protected DomainRepositoryAdapter(DomainPersistenceContexts contexts) {
        this.contexts = contexts;
    }

    // ------------------------------------------------------------------ conversions

    /** Converts a repository result (entity, Optional, collection, Page, Slice, Stream, …) to domain objects. */
    @SuppressWarnings("unchecked")
    protected <T> T fromJpa(Object result) {
        if (result == null) {
            return null;
        }
        MappingContext ctx = contexts.current();
        return (T) convertFromJpa(result, ctx);
    }

    private Object convertFromJpa(Object value, MappingContext ctx) {
        if (value == null) {
            return null;
        }
        PersistenceMappers mappers = ctx.mappers();
        if (mappers.isEntityInstance(value)) {
            return ctx.domainFor(value);
        }
        if (value instanceof Optional<?> opt) {
            return opt.map(v -> convertFromJpa(v, ctx));
        }
        if (value instanceof Page<?> page) {
            return page.map(v -> convertFromJpa(v, ctx));
        }
        if (value instanceof Slice<?> slice) {
            return slice.map(v -> convertFromJpa(v, ctx));
        }
        if (value instanceof Stream<?> stream) {
            return stream.map(v -> convertFromJpa(v, ctx));
        }
        if (value instanceof Set<?> set) {
            Set<Object> out = new LinkedHashSet<>(set.size());
            set.forEach(v -> out.add(convertFromJpa(v, ctx)));
            return out;
        }
        if (value instanceof Collection<?> col) {
            List<Object> out = new ArrayList<>(col.size());
            col.forEach(v -> out.add(convertFromJpa(v, ctx)));
            return out;
        }
        return value;
    }

    /** Converts a domain argument (object or collection of objects) to the corresponding JPA entities. */
    @SuppressWarnings("unchecked")
    protected <T> T toJpa(Object argument) {
        if (argument == null) {
            return null;
        }
        MappingContext ctx = contexts.current();
        if (ctx.mappers().isDomainInstance(argument)) {
            return (T) ctx.entityFor(argument);
        }
        if (argument instanceof Set<?> set) {
            Set<Object> out = new LinkedHashSet<>(set.size());
            set.forEach(v -> out.add(ctx.mappers().isDomainInstance(v) ? ctx.entityFor(v) : v));
            return (T) out;
        }
        if (argument instanceof Iterable<?> it) {
            List<Object> out = new ArrayList<>();
            it.forEach(v -> out.add(ctx.mappers().isDomainInstance(v) ? ctx.entityFor(v) : v));
            return (T) out;
        }
        return (T) argument;
    }

    // ------------------------------------------------------------------ writes

    /** Persists or updates the domain object; generated values are written back onto it. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <D> D save(JpaRepository repository, D domain) {
        MappingContext ctx = contexts.current();
        Object entity = ctx.entityFor(domain);
        Object saved = repository.save(entity);
        ctx.bindSaved(domain, saved);
        ctx.refreshDomainGraph(domain, saved);
        return domain;
    }

    protected <D> D saveAndFlush(JpaRepository<?, ?> repository, D domain) {
        D result = save(repository, domain);
        repository.flush();
        return result;
    }

    protected <D> List<D> saveAll(JpaRepository<?, ?> repository, Iterable<D> domains) {
        List<D> result = new ArrayList<>();
        for (D domain : domains) {
            result.add(save(repository, domain));
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void delete(JpaRepository repository, Object domain) {
        MappingContext ctx = contexts.current();
        Object entity = ctx.entityFor(domain);
        repository.delete(entity);
        ctx.forget(domain);
    }

    protected void deleteAll(JpaRepository<?, ?> repository, Iterable<?> domains) {
        for (Object domain : domains) {
            delete(repository, domain);
        }
    }
}
