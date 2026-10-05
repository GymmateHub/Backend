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
- **`@Version` optimistic locking.** This is deferred to its own change, because it needs:
  - a migration on every table;
  - mapper rules for a null version on detached domain objects, which Spring Data would otherwise treat as new;
  - a client-facing 409 contract.
- **Boot 4 / Modulith 2 / JJWT 0.13.** This is a separate upgrade track.

`Backend_v2/` has been deleted.

## Consequences

- New code has one template, and the build enforces it.
- Integration tests use Testcontainers (PostgreSQL 18) and therefore need Docker.
- **Known finding:** the Flyway schema has drifted from the entities, so Hibernate `ddl-auto=validate` fails (e.g. `api_rate_limits.is_active`). The integration tests use `update` until a reconciliation migration lands. This drift predates the refactor.
