package com.gymmate.shared.infrastructure.persistence;

import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Field-by-field mapping between a pure domain class and the JPA entity that persists it.
 *
 * <p>The two classes mirror each other: every instance field (including inherited ones) exists
 * on both sides with the same name. Values are copied as-is when the types are identical; fields
 * whose types are themselves a mapped domain/entity pair (associations) — or collections of them —
 * are converted through the {@link MappingContext}, which preserves identity across the graph.
 * The mirroring is validated by {@link PersistenceMappers} when all mappings are known.
 */
public final class EntityMapping<D, J> {

    private static final ObjenesisStd OBJENESIS = new ObjenesisStd(true);

    enum Kind { VALUE, ASSOCIATION, ASSOCIATION_COLLECTION }

    record FieldPair(String name, Field domainField, Field entityField, Kind kind) {
    }

    private final Class<D> domainType;
    private final Class<J> entityType;
    private final Map<String, Field> domainFields;
    private final Map<String, Field> entityFields;
    private List<FieldPair> pairs;
    private final Field domainIdField;

    EntityMapping(Class<D> domainType, Class<J> entityType) {
        this.domainType = domainType;
        this.entityType = entityType;
        this.domainFields = instanceFields(domainType);
        this.entityFields = instanceFields(entityType);
        this.domainIdField = domainFields.get(idFieldName(entityFields));
    }

    public Class<D> domainType() {
        return domainType;
    }

    public Class<J> entityType() {
        return entityType;
    }

    /**
     * Resolves field pairs against the full set of mappings and reports every mismatch.
     *
     * @param entityTypeForDomain entity type mapped to a domain type, or null (association targets)
     * @return human-readable problems; empty when the two classes mirror each other
     */
    List<String> resolve(Function<Class<?>, Class<?>> entityTypeForDomain) {
        List<String> problems = new ArrayList<>();
        List<FieldPair> resolved = new ArrayList<>();
        for (Map.Entry<String, Field> e : entityFields.entrySet()) {
            Field ef = e.getValue();
            Field df = domainFields.get(e.getKey());
            if (df == null) {
                problems.add(entityType.getSimpleName() + "." + e.getKey() + " has no counterpart in "
                        + domainType.getSimpleName());
                continue;
            }
            Kind kind = kindOf(df, ef, entityTypeForDomain);
            if (kind == null) {
                problems.add(entityType.getSimpleName() + "." + e.getKey() + " type " + ef.getGenericType().getTypeName()
                        + " does not mirror " + domainType.getSimpleName() + "." + e.getKey() + " type "
                        + df.getGenericType().getTypeName());
                continue;
            }
            resolved.add(new FieldPair(e.getKey(), df, ef, kind));
        }
        for (String name : domainFields.keySet()) {
            if (!entityFields.containsKey(name)) {
                problems.add(domainType.getSimpleName() + "." + name + " is not persisted by "
                        + entityType.getSimpleName());
            }
        }
        if (domainIdField == null) {
            problems.add(domainType.getSimpleName() + " has no counterpart of the @Id field of " + entityType.getSimpleName());
        }
        this.pairs = List.copyOf(resolved);
        return problems;
    }

    private static Kind kindOf(Field df, Field ef, Function<Class<?>, Class<?>> entityTypeForDomain) {
        if (df.getGenericType().equals(ef.getGenericType())) {
            return Kind.VALUE;
        }
        Class<?> mappedEntity = entityTypeForDomain.apply(df.getType());
        if (mappedEntity != null && mappedEntity.equals(ef.getType())) {
            return Kind.ASSOCIATION;
        }
        if (Collection.class.isAssignableFrom(df.getType()) && df.getType().equals(ef.getType())) {
            Class<?> dElem = elementType(df.getGenericType());
            Class<?> eElem = elementType(ef.getGenericType());
            if (dElem != null && eElem != null && eElem.equals(entityTypeForDomain.apply(dElem))) {
                return Kind.ASSOCIATION_COLLECTION;
            }
        }
        return null;
    }

    private static Class<?> elementType(Type type) {
        if (type instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 1
                && pt.getActualTypeArguments()[0] instanceof Class<?> c) {
            return c;
        }
        return null;
    }

    Object domainId(Object domain) {
        return ReflectionUtils.getField(domainIdField, domain);
    }

    D newDomain() {
        return instantiate(domainType);
    }

    J newEntity() {
        return instantiate(entityType);
    }

    /** Copies the domain state onto the entity (associations resolved through the context). */
    void copyToEntity(Object domain, Object entity, MappingContext ctx) {
        for (FieldPair p : pairs) {
            Object value = ReflectionUtils.getField(p.domainField(), domain);
            switch (p.kind()) {
                case VALUE -> ReflectionUtils.setField(p.entityField(), entity, value);
                case ASSOCIATION -> ReflectionUtils.setField(p.entityField(), entity, ctx.entityFor(value));
                case ASSOCIATION_COLLECTION -> copyCollection(p.entityField(), entity, (Collection<?>) value,
                        ctx::entityFor);
            }
        }
    }

    /** Copies the entity state onto the domain object (associations resolved through the context). */
    void copyToDomain(Object entity, Object domain, MappingContext ctx) {
        for (FieldPair p : pairs) {
            Object value = ReflectionUtils.getField(p.entityField(), entity);
            switch (p.kind()) {
                case VALUE -> ReflectionUtils.setField(p.domainField(), domain, value);
                case ASSOCIATION -> ReflectionUtils.setField(p.domainField(), domain, ctx.domainFor(value));
                case ASSOCIATION_COLLECTION -> copyCollection(p.domainField(), domain, (Collection<?>) value,
                        ctx::domainFor);
            }
        }
    }

    /** Copies only plain values (no associations) from entity to domain — used for post-write refresh. */
    void copyValuesToDomain(Object entity, Object domain) {
        for (FieldPair p : pairs) {
            if (p.kind() == Kind.VALUE) {
                ReflectionUtils.setField(p.domainField(), domain, ReflectionUtils.getField(p.entityField(), entity));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyCollection(Field targetField, Object target, Collection<?> source,
                                       Function<Object, Object> convert) {
        Collection existing = (Collection) ReflectionUtils.getField(targetField, target);
        if (source == null) {
            if (existing != null) {
                existing.clear();
            }
            return;
        }
        List<Object> converted = new ArrayList<>(source.size());
        for (Object item : source) {
            converted.add(convert.apply(item));
        }
        if (existing == null) {
            Collection fresh = java.util.Set.class.isAssignableFrom(targetField.getType())
                    ? new java.util.LinkedHashSet<>() : new ArrayList<>();
            fresh.addAll(converted);
            ReflectionUtils.setField(targetField, target, fresh);
            return;
        }
        if (sameElements(existing, converted)) {
            return; // keep managed collections untouched when nothing changed
        }
        // mutate in place: Hibernate tracks (orphan-removal) collections by instance
        existing.clear();
        existing.addAll(converted);
    }

    private static boolean sameElements(Collection<?> a, List<Object> b) {
        if (a.size() != b.size()) {
            return false;
        }
        int i = 0;
        for (Object x : a) {
            if (x != b.get(i++)) {
                return false;
            }
        }
        return true;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ReflectionUtils.makeAccessible(ctor);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            return OBJENESIS.newInstance(type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + type.getName(), e);
        }
    }

    /** Name of the entity's identifier field (@Id); defaults to "id". */
    private static String idFieldName(Map<String, Field> entityFields) {
        for (Field f : entityFields.values()) {
            if (f.isAnnotationPresent(jakarta.persistence.Id.class) || f.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                return f.getName();
            }
        }
        return "id";
    }

    private static Map<String, Field> instanceFields(Class<?> type) {
        Map<String, Field> fields = new LinkedHashMap<>();
        List<Class<?>> hierarchy = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            hierarchy.add(0, c);
        }
        for (Class<?> c : hierarchy) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                    continue;
                }
                ReflectionUtils.makeAccessible(f);
                fields.put(f.getName(), f);
            }
        }
        return fields;
    }
}
