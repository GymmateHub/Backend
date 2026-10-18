package com.gymmate.shared.infrastructure.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the pure domain class a JPA entity persists.
 *
 * <p>Every {@code @Entity} lives in a module's {@code internal.infrastructure.persistence}
 * package and mirrors its domain class field-for-field (same names and types; associations
 * point at the corresponding JPA entities). {@link PersistenceMappers} discovers the pairs
 * through this annotation and validates the mirroring at startup.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainModel {

    /** The domain class this entity persists. */
    Class<?> value();
}
