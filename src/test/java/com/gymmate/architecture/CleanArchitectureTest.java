package com.gymmate.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Horizontal (layer) rules of the Clean Architecture target, complementing the vertical
 * module boundaries verified by {@link com.gymmate.ModularityTests} (Spring Modulith).
 *
 * <p>Target layout per module:
 * <pre>
 * com.gymmate.&lt;module&gt;            public API (facades), dto/, events/
 *   internal.domain                pure Java: no Spring, no JPA, no infrastructure
 *   internal.application           use cases + ports; no web/persistence frameworks
 *   internal.infrastructure        web, persistence (JPA entities + Spring Data), messaging, integration, config
 * </pre>
 *
 * <p>The rules are enforced strictly: the refactor that introduced them (ADR 0002) burned the
 * recorded violation baseline down to zero, so any new violation fails the build.
 */
@AnalyzeClasses(packages = "com.gymmate", importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    /** The shared kernel must not know about any business module. */
    @ArchTest
    static final ArchRule shared_kernel_depends_on_no_module =
            noClasses().that().resideInAPackage("com.gymmate.shared..")
                    .should().dependOnClassesThat(
                            resideInAPackage("com.gymmate..").and(not(resideInAPackage("com.gymmate.shared.."))))
                    .as("shared kernel depends on no business module");

    /** Domain = plain Java. Frameworks and outer layers are forbidden. */
    @ArchTest
    static final ArchRule domain_is_framework_free =
            noClasses().that().resideInAPackage("com.gymmate..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.hibernate..",
                            "com.fasterxml.jackson..",
                            "jakarta.servlet..",
                            "com.gymmate..application..",
                            "com.gymmate..infrastructure..",
                            "com.gymmate..web..")
                    .as("domain layer is free of frameworks and outer layers");

    /** Use cases talk to ports, never to web or persistence technology directly. */
    @ArchTest
    static final ArchRule application_is_free_of_delivery_and_persistence_tech =
            noClasses().that().resideInAPackage("com.gymmate..application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "com.gymmate..infrastructure..",
                            "com.gymmate..web..",
                            "org.springframework.web..",
                            "org.springframework.data.jpa..",
                            "jakarta.persistence..",
                            "jakarta.servlet..")
                    .as("application layer does not use web or persistence technology");

    /** Inward-only dependency flow inside each module. */
    @ArchTest
    static final ArchRule layers_are_respected =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true)
                    .layer("Domain").definedBy("com.gymmate..internal.domain..")
                    .layer("Application").definedBy("com.gymmate..internal.application..")
                    .layer("Infrastructure").definedBy("com.gymmate..internal.infrastructure..")
                    .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure");

    /** Delivery adapters live in infrastructure.web. */
    @ArchTest
    static final ArchRule controllers_live_in_infrastructure_web =
            classes().that().areAnnotatedWith(org.springframework.stereotype.Controller.class)
                    .or().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .or().areAnnotatedWith(org.springframework.web.bind.annotation.RestControllerAdvice.class)
                    .or().areAnnotatedWith(org.springframework.web.bind.annotation.ControllerAdvice.class)
                    .should().resideInAPackage("com.gymmate..infrastructure.web..")
                    .as("controllers live in infrastructure.web");

    /** JPA mapping is a persistence concern (strict domain purity). */
    @ArchTest
    static final ArchRule jpa_types_live_in_infrastructure_persistence =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .or().areAnnotatedWith(jakarta.persistence.MappedSuperclass.class)
                    .or().areAnnotatedWith(jakarta.persistence.Embeddable.class)
                    .should().resideInAPackage("com.gymmate..infrastructure.persistence..")
                    .as("JPA entities live in infrastructure.persistence");

    /** A module's public API (facades, DTOs, events, SPIs) never exposes or uses its internals. */
    @ArchTest
    static final ArchRule public_api_does_not_depend_on_internals =
            noClasses().that().resideInAPackage("com.gymmate.*.api..")
                    .should().dependOnClassesThat().resideInAPackage("com.gymmate.*.internal..")
                    .as("module APIs do not depend on module internals");

    /** Every JPA entity declares the domain class it persists (data-mapper pairing). */
    @ArchTest
    static final ArchRule entities_declare_their_domain_model =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().beAnnotatedWith(com.gymmate.shared.infrastructure.persistence.DomainModel.class)
                    .as("JPA entities declare their @DomainModel");

    /** Spring Data repositories are persistence adapters, hidden behind application ports. */
    @ArchTest
    static final ArchRule spring_data_repositories_live_in_infrastructure_persistence =
            classes().that().areInterfaces()
                    .and(assignableTo(org.springframework.data.repository.Repository.class))
                    .should().resideInAPackage("com.gymmate..infrastructure.persistence..")
                    .as("Spring Data repositories live in infrastructure.persistence");
}
