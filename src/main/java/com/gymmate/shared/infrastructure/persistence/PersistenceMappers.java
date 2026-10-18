package com.gymmate.shared.infrastructure.persistence;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all domain ⇄ JPA entity mappings, discovered from {@link DomainModel} annotations.
 *
 * <p>Construction validates that every pair mirrors field-for-field and fails fast otherwise, so a
 * field added to a domain class but not to its entity (or vice versa) breaks application startup
 * and the persistence-mapping architecture test rather than silently dropping data.
 */
@Component
public class PersistenceMappers {

    static final String BASE_PACKAGE = "com.gymmate";

    private final Map<Class<?>, EntityMapping<?, ?>> byDomain = new HashMap<>();
    private final Map<Class<?>, EntityMapping<?, ?>> byEntity = new HashMap<>();

    public PersistenceMappers() {
        this(scan(BASE_PACKAGE));
    }

    PersistenceMappers(Collection<Class<?>> entityTypes) {
        for (Class<?> entityType : entityTypes) {
            DomainModel model = entityType.getAnnotation(DomainModel.class);
            if (model == null) {
                throw new IllegalArgumentException(entityType.getName() + " is not annotated with @DomainModel");
            }
            register(model.value(), entityType);
        }
        List<String> problems = new ArrayList<>();
        for (EntityMapping<?, ?> mapping : byDomain.values()) {
            problems.addAll(mapping.resolve(this::entityTypeFor));
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Domain/JPA mapping mismatches:\n  - " + String.join("\n  - ", problems));
        }
    }

    /** All entity classes annotated with {@link DomainModel} below {@code basePackage}. */
    public static List<Class<?>> scan(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DomainModel.class));
        List<Class<?>> types = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            types.add(ClassUtils.resolveClassName(bd.getBeanClassName(), PersistenceMappers.class.getClassLoader()));
        }
        return types;
    }

    private <D, J> void register(Class<D> domainType, Class<J> entityType) {
        EntityMapping<D, J> mapping = new EntityMapping<>(domainType, entityType);
        if (byDomain.putIfAbsent(domainType, mapping) != null) {
            throw new IllegalStateException("Domain type " + domainType.getName() + " is persisted by more than one entity");
        }
        byEntity.put(entityType, mapping);
    }

    Class<?> entityTypeFor(Class<?> domainType) {
        EntityMapping<?, ?> m = byDomain.get(domainType);
        return m == null ? null : m.entityType();
    }

    /** @return the mapping for a domain instance's class, or null when it is not a mapped domain type */
    EntityMapping<?, ?> forDomain(Class<?> domainType) {
        return byDomain.get(domainType);
    }

    /** @return the mapping for an entity instance's class (proxies unwrapped), or null */
    EntityMapping<?, ?> forEntity(Object entity) {
        return byEntity.get(Hibernate.getClass(entity));
    }

    boolean isDomainInstance(Object value) {
        return value != null && byDomain.containsKey(value.getClass());
    }

    boolean isEntityInstance(Object value) {
        return value != null && byEntity.containsKey(Hibernate.getClass(value));
    }

    public int size() {
        return byDomain.size();
    }
}
