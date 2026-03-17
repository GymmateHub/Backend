package com.gymmate.shared.security.service;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for validating tenant isolation and preventing cross-tenant data access.
 * All service methods that access tenant-scoped data should use this service
 * to ensure users can only access data from their own organization.
 */
@Service
@Slf4j
public class TenantValidationService {

    /**
     * Validates that the given organisation ID matches the current tenant context.
     * Throws an exception if there's a mismatch (cross-tenant access attempt).
     *
     * @param entityOrganisationId The organisation ID of the entity being accessed
     * @param entityType The type of entity (for logging)
     * @param entityId The ID of the entity (for logging)
     * @throws DomainException if the organisation ID doesn't match current tenant
     */
    public void validateTenantAccess(UUID entityOrganisationId, String entityType, UUID entityId) {
        UUID currentTenantId = TenantContext.getCurrentTenantId();

        if (currentTenantId == null) {
            log.warn("Tenant context not set - possible SUPER_ADMIN access or misconfiguration");
            // Allow SUPER_ADMIN to bypass (tenant context is null for SUPER_ADMIN)
            return;
        }

        if (entityOrganisationId == null) {
            log.error("Entity {} with id {} has null organisationId - data integrity issue",
                    entityType, entityId);
            throw new DomainException("DATA_INTEGRITY_ERROR",
                    "Entity does not have an organisation assigned");
        }

        if (!entityOrganisationId.equals(currentTenantId)) {
            log.error("Cross-tenant access attempt detected! User from org {} tried to access {} {} from org {}",
                    currentTenantId, entityType, entityId, entityOrganisationId);
            throw new DomainException("UNAUTHORIZED_ACCESS",
                    "You do not have permission to access this resource");
        }

        log.debug("Tenant validation passed for {} {} in org {}", entityType, entityId, currentTenantId);
    }

    /**
     * Validates that a gym belongs to the current tenant's organisation.
     *
     * @param gymOrganisationId The organisation ID that owns the gym
     * @param gymId The gym ID
     */
    public void validateGymAccess(UUID gymOrganisationId, UUID gymId) {
        validateTenantAccess(gymOrganisationId, "Gym", gymId);
    }

    /**
     * Validates multiple entities belong to the current tenant.
     *
     * @param entities Array of [organisationId, entityType, entityId] triplets
     */
    public void validateMultipleTenantAccess(Object[]... entities) {
        for (Object[] entity : entities) {
            if (entity.length != 3) {
                throw new IllegalArgumentException("Each entity must have [orgId, type, id]");
            }
            validateTenantAccess((UUID) entity[0], (String) entity[1], (UUID) entity[2]);
        }
    }

    /**
     * Gets the current tenant ID, throwing an exception if not set.
     * Use this when tenant context is required.
     *
     * @return Current tenant ID
     * @throws DomainException if tenant context is not set
     */
    public UUID requireCurrentTenantId() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new DomainException("TENANT_CONTEXT_REQUIRED",
                    "This operation requires a tenant context");
        }
        return tenantId;
    }

    /**
     * Validates that the provided ID matches the current tenant ID.
     * Use this when an API accepts an organisation/gym ID parameter.
     *
     * @param providedId The organisation ID provided in the request
     * @throws DomainException if the ID doesn't match current tenant
     */
    public void validateProvidedTenantId(UUID providedId) {
        UUID currentTenantId = requireCurrentTenantId();

        if (!providedId.equals(currentTenantId)) {
            log.error("User from org {} attempted to access org {} data",
                    currentTenantId, providedId);
            throw new DomainException("UNAUTHORIZED_ACCESS",
                    "You can only access data from your own organisation");
        }
    }
}
