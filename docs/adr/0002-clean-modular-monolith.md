# ADR 0002: Clean Architecture modular monolith (strict domain, hard-enforced boundaries)

**Status:** Accepted. This supersedes the debt tiers in ADR 0001.
**Date:** 2026-10-05

## Context

ADR 0001 got `ApplicationModules.verify()` passing by granting wide `@NamedInterface`s
and marking `shared` and `subscription` as `Type.OPEN`. An ArchUnit ratchet recorded
2,228 Clean Architecture violations. A parallel rewrite (`Backend_v2`, Boot 4 /
Modulith 2) was started and then abandoned. It did not compile, and it was not a
Clean/Hexagonal design either. This ADR records the refactor that brought `Backend`
itself to the target. The full plan and gap analysis is in
`docs/refactor/CLEAN_MODULAR_MONOLITH_PLAN.md`.

## Decisions

### 1. Module map: 14 modules plus a shared kernel

| Module | Absorbed |
|---|---|
| `shared` (kernel, the only shared module) | base types, multitenancy, error model, `CurrentUser`, event infrastructure |
| `identity` | `user` + `shared.security.*` (auth, JWT, OTP, invites, security chain) |
| `onboarding` | owner/member registration flow, `PendingRegistration` |
| `organisation` | `organisation` + `gym` |
| `billing` | `payment` + `subscription` (this removes the `Type.OPEN` cycle workaround) |
| `membership`, `scheduling`, `access`, `health`, `notification`, `whitelabel`, `ai` | unchanged scope |
| `retail` | `inventory` + `pos` |
| `crm` | `lead` |
| `reporting` | `analytics` + `admin` |

`@Modulithic(systemName = "GymMateHub", sharedModules = "shared")`. No module is `OPEN`.

### 2. Per-module layout

```
<module>/
  api/            public facade interfaces (<Module>Api), api/dto (records), api/event, api/spi
  internal/
    domain/                     pure Java: no Spring, JPA, Jackson or web imports
    application/                use cases, application/port (outbound ports), application/dto
    infrastructure/web          controllers
    infrastructure/persistence  <X>JpaEntity, Spring Data repo, <X>RepositoryAdapter
    infrastructure/messaging    event listeners
    infrastructure/integration  Stripe, SMTP, SNS, Meta, OpenAI adapters
```

Other modules may use only `api..`. Nothing under `internal` is a named interface.

### 3. Strict domain purity via a data mapper (shared.infrastructure.persistence)

Every aggregate has a pure domain class and a `@DomainModel(X.class) XJpaEntity`
mirror. Our own framework does the mapping:

- `EntityMapping`: a reflective field copy, including associations and collections.
- `PersistenceMappers`: classpath scan plus mirror validation at startup, which fails fast.
- `MappingContext`: a per-Hibernate-session identity map. Hibernate integrator hooks
  sync domain changes into entities before (auto-)flush and refresh generated
  values after insert/update.

The effect is that an application service keeps its usual JPA-style programming
model: load, mutate the domain object, commit. There is no hand-written mapper per
aggregate. `DomainRepositoryAdapter` is the base for every repository adapter.

### 4. Cross-module collaboration

- **Synchronous queries and commands** go through public facades: `IdentityApi`,
  `OrganisationApi`, `MembershipApi`, `EmailApi`, `NotificationApi`, `WhitelabelApi`,
  `ClassesFacade`, `InventoryFacade`, `PosFacade`, `UserQueryPort`.
- **Dependency inversion** for the remaining cycles:
  - `GymDirectory` SPI.
  - Billing's `GymPaymentAccountPort` and `OrganisationBillingInfoProvider`, implemented by adapters over `OrganisationApi`.
- **Side effects** use events in `<module>.api.event`. These events are JSON-serialisable records/classes and implement `TenantAwareEvent`.

### 5. Durable events

Async cross-module listeners use `@AsyncModuleListener`, which combines `@Async`, `REQUIRES_NEW` and `@TransactionalEventListener(AFTER_COMMIT, fallbackExecution = true)`.
Delivery is recorded in the Spring Modulith JDBC event publication registry (Flyway
`V18__Event_Publication_Registry.sql`). Incomplete publications are republished on
restart.

`IntegrationEventSerializationTest` round-trips every public event.
`DurableEventDeliveryIntegrationTest` covers three cases: after-commit delivery, no delivery on rollback, and non-transactional publishing.

**Documented synchronous exceptions.** These must share the publisher's transaction, so they remain `@EventListener`:

- `MembershipPaymentEventListener`: membership state must change atomically with the payment record (see ADR 0001 §5).
- `OrganisationProvisioningListener`: creates the starter trial subscription in the same transaction as the organisation.

### 6. Enforcement (hard, no baseline)

- `ModularityTests`: `ApplicationModules.verify()`.
- `CleanArchitectureTest`, which covers:
  - domain purity, and layer direction;
  - controllers in `infrastructure.web`;
  - JPA types and repositories in `infrastructure.persistence`;
  - the kernel depends on no module;
  - `api` never depends on `internal`;
  - every `@Entity` declares its `@DomainModel`.
- `<Module>ModuleTest` (one per module, `@ApplicationModuleTest`): boots the module on its own
  against PostgreSQL. Other modules are not started; the test mocks only the public APIs the
  module uses and the SPIs it declares. A hidden dependency, such as a bean, a JPQL join on
  another module's entity, or a kernel bean that needs a module, makes the test fail to boot.
  Scenario tests cover the key cross-module contracts:
  - organisation publishes `OrganisationCreatedEvent`;
  - billing provisions the starter trial when it receives that event.

The frozen ArchUnit store was burnt down from 2,228 to 0 and deleted, so any new violation fails the build.

`ApiEndpointSnapshotTest` keeps the HTTP contract fixed (377 routes, unchanged by this refactor).

### 7. Backend_v2 salvage, then deletion

Ported from V2:

- **Cross-gym access rule.** A member may enter any gym of their own organisation. A door of another organisation is denied with `DenyReason.FOREIGN_ORGANISATION` (`AccessService`, tests in `AccessServiceTest`).
- **`@Modulithic(sharedModules)`.**
- **Records for public DTOs.**
- **Freeze end-date validation.** The end date must be in the future (`FREEZE_DATE_INVALID`). Backend's other freeze rules were already stricter than V2's.

Considered and not ported:

- **V2 onboarding status machine.** It is covered by the existing user-status / OTP flow in `onboarding` + `identity`.
- **`@Version` optimistic locking.** This landed as its own change (see §8).
- **Boot 4 / Modulith 2 / JJWT 0.13.** This was done as a separate upgrade (see §9).

`Backend_v2/` has been deleted.

### 8. Optimistic locking

`BaseJpaEntity` carries a JPA `@Version`, and `V20` adds the `version` column to every
aggregate table (existing rows start at 0). Three rules follow:

- Concurrent transactions that update the same row: the second one fails at commit.
- The data mapper never copies a version onto a managed entity. Saving a domain copy whose
  version is older than the stored row is rejected.
- Hand-built copies with no version are not checked.

Failures map to HTTP 409. The domain `BaseEntity` exposes the version, so clients can see it
(for example, to add `If-Match` later).

### 9. Platform upgrade: Spring Boot 4.1

The upgrade moved these components:

| Component | From | To |
|---|---|---|
| Spring Boot | 3.5 | 4.1.1 |
| Spring Framework | 6 | 7 |
| Spring Security | 6 | 7 |
| Hibernate | 6.6 | 7 |
| Jackson | 2 | 3 (`tools.jackson`) |
| Spring Modulith | 1.4 | 2.1.1 |
| Spring AI | 1.0.0-M1 | 2.0.1 GA |
| Testcontainers | 1.21 | 2.0 |
| springdoc | 2.8 | 3.0 |
| JJWT | 0.12 | 0.13 |
| Redisson | 3.35 | 4.8 |
| ArchUnit | 1.4 | 1.5 |

The Stripe SDK is deliberately unchanged; a Stripe major version bump is its own change.

Notable decisions:

- **Starters.** Boot 4's modular starters are used (`webmvc`, `flyway`, `restclient`, and the
  matching test starters).
- **Jackson 2 compatibility.** The shared `JacksonConfig` restores Jackson 2's creator
  visibility, so request DTOs built with Lombok `@Data @Builder` keep binding.
- **Retail enum binding.** Retail's enum mix-ins moved to `JsonMapperBuilderCustomizer`.
- **Security headers filter.** It is anchored before `DisableEncodeUrlFilter`, the first
  filter in the chain, because Security 7 removed `ChannelProcessingFilter`.
- **Persistence listeners.** They key the mapping context by
  `SharedSessionContractImplementor`, which is what Hibernate 7 post-events expose.
- **Event registry migration.** `V21` adds Modulith 2's registry columns (`status`,
  `completion_attempts`, `last_resubmission_date`) and back-fills existing publications.
- **Swagger annotations.** There is now one `swagger-annotations` version; Spring AI's javax
  copy is excluded. `OpenApiDocumentIntegrationTest` guards `/v3/api-docs`.
- **Gson.** Gson is declared explicitly. The Stripe webhook code uses it and Boot 4 no longer
  brings it in transitively.

### 10. Generic repository ports and adapters

The CRUD contract is declared and implemented once.

- **The contract:** `shared.application.port.DomainRepository<D, ID>`. It covers `save`,
  `saveAll`, `findById`, `existsById`, `findAll`, paged `findAll`, `findAllById`, `count`,
  `deleteById` and `delete`.
- **The implementation:** `shared.infrastructure.persistence.JpaDomainRepositoryAdapter<D, ID, R>`,
  built on the module's Spring Data repository `R`. Subclasses reach `R` as `jpaRepository`.

Module ports extend `DomainRepository` and adapters extend the base, so each declares only
its own finders. The repositories' Spring Data interfaces are unchanged.

`flush`, `saveAndFlush` and `deleteAll` were dropped from the contract because nothing called
them. Health aggregates use `SoftDeletingJpaDomainRepositoryAdapter`, where every delete path
deactivates the row (previously `deleteById` hard-deleted it). Net effect: about 6,000 fewer
lines across 62 ports and 62 adapters.

## Consequences

- New code has one template, and the build enforces it.
- Integration tests use Testcontainers (PostgreSQL 18) and therefore need Docker.
- **Schema is owned by Flyway.** The migrations and the entity mappings had drifted before
  this refactor:
  - the audit columns `is_active` and `updated_by` were missing;
  - `created_by` was a UUID column instead of text;
  - `member_memberships` mapped `plan_id` while the schema had `membership_plan_id`;
  - some columns and indexes were missing.

  Only `ddl-auto=update` kept the app running, and the `dev` and `docker` profiles (which use
  `validate`) could not start. `V19__Reconcile_Schema_With_Entity_Mappings` fixes this. It is
  idempotent, so it is safe on databases Hibernate already patched. Integration tests now run
  with `ddl-auto=validate`, so new drift fails the build.
- `docker compose up --build` runs the full stack (PostgreSQL 18, Redis, backend with
  Flyway + validate).
