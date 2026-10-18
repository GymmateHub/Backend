package com.gymmate.crm;

import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


/**
 * Boots the crm module on its own (plus the shared kernel) against PostgreSQL.
 * Mocks the one public API it uses: {@code organisation.OrganisationApi} (gym ownership checks).
 */
@ApplicationModuleTest
class CrmModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    OrganisationApi organisationApi;

    @Test
    void bootstrapsInIsolation() {
    }
}
