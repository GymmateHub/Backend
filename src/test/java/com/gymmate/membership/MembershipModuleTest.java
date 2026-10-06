package com.gymmate.membership;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the membership module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses:
 * {@code identity.IdentityApi}, {@code organisation.OrganisationApi}.
 */
@ApplicationModuleTest
class MembershipModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @MockitoBean
    OrganisationApi organisationApi;

    @Test
    void bootstrapsInIsolation() {
    }
}
