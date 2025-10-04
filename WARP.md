# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Project overview
- Language/Framework: Java 21, Spring Boot 3.3.x
- Build tool: Maven
- Style: Modular monolith organized by feature modules with consistent internal layering

How to build, run, and test
Prerequisites
- Ensure JDK 21 and Maven are installed (README notes Maven Wrapper may be added later, but it is not present currently).

Common commands
- Build the project
  - mvn clean package
- Run the application (dev)
  - mvn spring-boot:run
- Run all tests
  - mvn test
- Run a single test class
  - mvn -Dtest=ClassNameTest test
- Run a single test method
  - mvn -Dtest=ClassNameTest#methodName test

Notes
- Lint/format: There is no code style plugin configured in Maven. An .editorconfig is present; rely on IDE/editor formatting consistent with that file.
- Server port: 8080 (see src/main/resources/application.yml)
- In-memory DB for dev: H2 (PostgreSQL compatibility mode), JPA ddl-auto=validate, Flyway enabled
- DB migrations: Place SQL files under src/main/resources/db/migration (classpath:db/migration)

High-level architecture
Modular monolith with vertical feature modules and a shared cross-cutting module:
- Feature modules (from README and directory structure): analytics, booking, inventory, membership, notification, payment, user
  - Each feature module is structured into four layers:
    - api: HTTP layer (controllers/DTOs/validation)
    - application: use cases, orchestration, transactional boundaries
    - domain: core domain model (entities, value objects, domain services, repository interfaces)
    - infrastructure: adapters (e.g., persistence via JPA, external integrations, configuration)
- shared module: cross-cutting concerns such as config, exception handling, security primitives, utilities, and shared DTOs

Bootstrapping and runtime
- Entry point: src/main/java/com/gymmate/GymMateApplication.java (annotated with @SpringBootApplication)
- Configuration: src/main/resources/application.yml
  - spring.application.name: gymmate-backend
  - H2 datasource configured for development
  - Flyway enabled, scanning classpath:db/migration
  - Management endpoints exposed: health, info

Key dependencies and tooling (from pom.xml)
- Spring Boot starters: web, validation, actuator
- Security and JPA starters included (security, data-jpa)
- Databases: PostgreSQL (runtime), H2 (runtime for dev)
- Flyway for DB migrations
- Lombok (optional) and MapStruct (optional) with annotation processors configured
- Testing: spring-boot-starter-test

Conventions and expectations
- Tests (when added) should live under src/test/java and can be executed with mvn test or filtered via -Dtest.
- Schema changes should be managed via Flyway by adding versioned SQL files to src/main/resources/db/migration.
- Domain logic belongs in the domain layer of each feature module; infrastructure should depend on domain-defined interfaces (hexagonal/clean boundaries within each module).
