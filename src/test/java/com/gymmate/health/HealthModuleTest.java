package com.gymmate.health;

import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;


/**
 * Boots the health module on its own (plus the shared kernel) against PostgreSQL.
 * The module depends on no other module.
 */
@ApplicationModuleTest
class HealthModuleTest extends ModuleIntegrationTest {

    @Test
    void bootstrapsInIsolation() {
    }
}
