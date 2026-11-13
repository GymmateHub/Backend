package com.gymmate.configuration;

import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * Boots the configuration module on its own (plus the shared kernel) against PostgreSQL.
 */
@ApplicationModuleTest
class ConfigurationModuleTest extends ModuleIntegrationTest {

    @Test
    void bootstrapsInIsolation() {
    }
}
