package com.gymmate.ai;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.notification.api.NotificationApi;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the ai module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses:
 * {@code identity.IdentityApi}, {@code notification.NotificationApi}, {@code organisation.OrganisationApi}.
 */
@ApplicationModuleTest
class AiModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @MockitoBean
    NotificationApi notificationApi;

    @MockitoBean
    OrganisationApi organisationApi;

    @Test
    void bootstrapsInIsolation() {
    }
}
