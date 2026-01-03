package com.gymmate.shared.multitenancy;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Aspect that enables Hibernate tenant filters before repository operations.
 * This ensures all queries are automatically scoped to the current tenant.
 *
 * The filters are enabled based on the current TenantContext:
 * - tenantFilter: Always enabled when organisationId is present
 * - gymFilter: Only enabled when gymId is present (for gym-scoped operations)
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterAspect {

    private final EntityManager entityManager;

    /**
     * Enable tenant filters before any repository method execution.
     * Matches all Spring Data JPA repository methods.
     */
    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void enableTenantFilter() {
        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        if (organisationId != null) {
            Session session = entityManager.unwrap(Session.class);

            // Enable organisation-level tenant filter
            session.enableFilter("tenantFilter")
                   .setParameter("organisationId", organisationId);

            log.trace("Enabled tenantFilter for organisation: {}", organisationId);

            // Enable gym-level filter if gym context is set
            if (gymId != null) {
                session.enableFilter("gymFilter")
                       .setParameter("gymId", gymId);

                log.trace("Enabled gymFilter for gym: {}", gymId);
            }
        }
    }

    /**
     * Enable tenant filter specifically for custom repository queries.
     * Matches the gymmate package repositories.
     */
    @Before("execution(* com.gymmate..infrastructure..*Repository+.*(..))")
    public void enableTenantFilterForCustomRepos() {
        enableTenantFilter();
    }
}

