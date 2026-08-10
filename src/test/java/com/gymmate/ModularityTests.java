package com.gymmate;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Boundary-enforcement gate for the module structure declared via
 * {@code package-info.java} across {@code com.gymmate.*}. Pure bytecode/reflection
 * analysis — no Spring context, no DB — so this runs fast under the same
 * {@code ./mvnw -B test} CI step as every other test. A PR that introduces a new
 * illegal cross-module internal-package reach fails this the same way a broken unit
 * test would.
 */
class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(GymMateApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesDocumentationSnapshot() {
        new Documenter(modules)
                .writeDocumentation()
                .writeModuleCanvases();
    }

    @Test
    void printsModuleModel() {
        modules.forEach(System.out::println);
    }
}
