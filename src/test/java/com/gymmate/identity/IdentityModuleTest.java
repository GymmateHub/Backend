package com.gymmate.identity;

import com.gymmate.notification.api.EmailApi;
import com.gymmate.identity.api.spi.GymDirectory;
import com.gymmate.identity.api.spi.MemberLimitGuard;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the identity module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses
 * and of the SPIs it declares (implemented by other modules):
 * {@code notification.EmailApi}, {@code identity.GymDirectory}, {@code identity.MemberLimitGuard}.
 */
@ApplicationModuleTest
class IdentityModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    EmailApi emailApi;

    @MockitoBean
    GymDirectory gymDirectory;

    @MockitoBean
    MemberLimitGuard memberLimitGuard;

    @Test
    void bootstrapsInIsolation() {
    }
}
