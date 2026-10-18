package com.gymmate.health;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


/**
 * Boots the health module on its own (plus the shared kernel) against PostgreSQL.
 * Mocks the one public API it uses: {@code identity.IdentityApi} (a member's own health data).
 */
@ApplicationModuleTest
class HealthModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @Test
    void bootstrapsInIsolation() {
    }
}
