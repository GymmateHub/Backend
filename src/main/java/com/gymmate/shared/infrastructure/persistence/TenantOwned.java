package com.gymmate.shared.infrastructure.persistence;

import java.util.UUID;

/**
 * A persisted row owned by one organisation. {@link JpaDomainRepositoryAdapter} hides rows of
 * other organisations from id lookups, which Hibernate's tenant filter does not cover.
 */
public interface TenantOwned {

    UUID getOrganisationId();
}
