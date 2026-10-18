package com.gymmate.scheduling;

import com.gymmate.membership.api.MembershipApi;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the scheduling module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses:
 * {@code membership.MembershipApi}.
 */
@ApplicationModuleTest
class SchedulingModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    MembershipApi membershipApi;

    @Test
    void bootstrapsInIsolation() {
    }
}
