package com.gymmate.reporting;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.membership.api.MembershipApi;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.retail.api.InventoryFacade;
import com.gymmate.retail.api.PosFacade;
import com.gymmate.scheduling.api.ClassesFacade;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the reporting module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses:
 * {@code identity.IdentityApi}, {@code membership.MembershipApi}, {@code organisation.OrganisationApi}, {@code retail.InventoryFacade}, {@code retail.PosFacade}, {@code scheduling.ClassesFacade}.
 */
@ApplicationModuleTest
class ReportingModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @MockitoBean
    MembershipApi membershipApi;

    @MockitoBean
    OrganisationApi organisationApi;

    @MockitoBean
    InventoryFacade inventoryFacade;

    @MockitoBean
    PosFacade posFacade;

    @MockitoBean
    ClassesFacade classesFacade;

    @Test
    void bootstrapsInIsolation() {
    }
}
