package com.gymmate.shared.events;

import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asynchronous, durable cross-module event listener.
 *
 * <p>Like Spring Modulith's {@code @ApplicationModuleListener}: runs asynchronously in its own
 * transaction <em>after</em> the publishing transaction committed, and is recorded in the
 * {@code event_publication} registry so failed or interrupted deliveries are retried
 * (republished on restart). Additionally, events published outside a transaction are still
 * delivered ({@code fallbackExecution}), preserving the behaviour of the plain
 * {@code @Async @EventListener}s this replaces.
 *
 * <p>Listeners must take tenant context from the event ({@code TenantAwareEvent}), never from
 * the publishing thread.
 */
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface AsyncModuleListener {

    /** Optional stable listener id (defaults to the method signature). */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "id")
    String id() default "";
}
