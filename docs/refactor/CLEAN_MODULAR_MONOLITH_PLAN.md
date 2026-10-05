# Clean Modular Monolith Refactor — Gap Analysis & Execution Plan

> **Status (2026-10-05): complete.** Phases 0–7 are done on `restructure`. The outcome:
>
> - 14 modules plus the kernel.
> - The ArchUnit debt went from 2,228 to 0, and the rules are now enforced hard.
> - `verify()` passes with no `OPEN` module.
> - Events are durable, backed by the JDBC registry.
> - The API snapshot is unchanged (377 routes).
> - `Backend_v2/` is deleted.
>
> Deviations and the final decisions are recorded in [ADR 0002](../adr/0002-clean-modular-monolith.md). The
> definition of done (§6) is met. The exceptions are `@Version` (deferred) and per-module
> `@ApplicationModuleTest`, which is replaced by Testcontainers integration tests.

**Scope:** Bring `Backend/` (branch `restructure`) to a Clean Architecture + Spring Modulith modular monolith, salvage what is worth keeping from `Backend_v2/`, then delete `Backend_v2/`.
**Assessed:** 2026-10-05, against `restructure` @ `cf89a84` (Phase 0 safety net).

---

## 1. Where things actually stand

### Backend (the target codebase)

| Metric | Value |
|---|---|
| Main sources | 737 Java files, ~49k LOC, 18 top-level packages (1 empty: `classes/`) |
| REST surface | 45 controllers, 377 routes (snapshotted) |
| Persistence | 67 JPA types (63 of them sit in `domain/` packages), 62 Spring Data repos, 18 Flyway migrations, 62 tables |
| Tests | **527 run, 0 failures, 3 skipped** (Testcontainers IT, needs Docker) — build green |
| Modulith | `ApplicationModules.verify()` **passes**, but only because of wide `@NamedInterface` grants and two `Type.OPEN` modules (`shared`, `subscription`) |
| ArchUnit (frozen ratchet) | **2,228 recorded violations** across 6 rules (table below) |
| Events | 17 `publishEvent` calls, 10 plain `@EventListener`s, **0 `@ApplicationModuleListener`**, no event publication registry (not durable) |

Two different internal layouts coexist today:

- **Layer-first** (`domain/ application/ infrastructure/ + internal/web`): user, gym, organisation, membership, notification, payment, subscription, whitelabel, lead, ai
- **`internal/{domain,repository,service,web}`**: access, scheduling, health, inventory, pos, admin, analytics

Neither is Clean Architecture yet: domain classes are JPA entities, services use Spring Data directly, controllers aren't in `infrastructure.web`.

### Frozen ArchUnit debt by module (what the refactor must burn down to zero)

| Module | JPA placement | Controller placement | App uses JPA/web | Domain impure | Repo placement | Shared→module | **Total** |
|---|---:|---:|---:|---:|---:|---:|---:|
| shared | 7 | 3 | 0 | 71 | 3 | **343** | **427** |
| payment | 6 | 5 | 85 | 140 | 6 | – | 242 |
| user | 5 | 5 | 108 | 103 | 5 | – | 226 |
| notification | 6 | 5 | 81 | 89 | 6 | – | 187 |
| membership | 5 | 3 | 87 | 84 | 5 | – | 184 |
| subscription | 4 | 1 | 53 | 113 | 4 | – | 175 |
| inventory | 6 | 4 | 0 | 136 | 6 | – | 152 |
| health | 8 | 5 | 0 | 106 | 8 | – | 127 |
| gym | 2 | 1 | 26 | 52 | 1 | – | 82 |
| pos | 3 | 1 | 0 | 74 | 3 | – | 81 |
| access | 6 | 1 | 0 | 67 | 6 | – | 80 |
| scheduling | 5 | 5 | 0 | 63 | 5 | – | 78 |
| organisation | 1 | 1 | 43 | 25 | 1 | – | 71 |
| whitelabel | 1 | 1 | 13 | 40 | 1 | – | 56 |
| lead | 1 | 1 | 11 | 15 | 1 | – | 29 |
| ai | 1 | 1 | 17 | 8 | 1 | – | 28 |
| admin / analytics | – | 1 each | – | – | – | – | 2 |

("App uses JPA/web" is 0 for the `internal/service` modules only because the rule looks for `..application..` packages they don't have yet. Once they move, their services will show up there too.)

### Cross-module coupling hot spots (imports of another module's non-API packages)

- **`shared` → `user` (32), `gym` (6), `organisation` (4), `notification` (3), `subscription` (1).** Auth/identity (`shared.security.{api,service,domain,repository,dto}`) lives in the shared kernel and reaches into `user`. This is the single biggest structural defect: the kernel isn't a kernel.
- **`user` / `gym` / `organisation` / `membership` domain + infrastructure** are imported by 8+ modules (ai, admin, access, analytics, payment, membership, organisation). This is the ADR-0001 "Tier-1" debt.
- **`payment` → `subscription.domain` (5)**: payment writes `Subscription` directly; `subscription` is `Type.OPEN` to hide the cycle.
- **`notification` → `whitelabel.application/domain` (10)**.
- `shared.constants` holds 18 module-owned enums (`BookingStatus`, `InvoiceStatus`, `RefundStatus`, `SubscriptionStatus`, …).
- Good news: cross-module JPA associations are almost nonexistent. Modules already reference each other by UUID (only `PasswordResetToken → User`, which moves with identity anyway). That makes module extraction much cheaper.

### Backend_v2

- Spring Boot **4.1.1**, Modulith **2.1.1**, JJWT 0.13, package `com.gymmatehub`, 86 files, 4 modules (user, organisation, onboarding, membership), 9 tables, all of which already exist in Backend.
- **It does not compile** (Lombok/`Optional` errors in `User`), and it isn't under git.
- Its layering is `internal/{domain,repository,service,web}` with JPA entities as domain objects. It's Modulith-style, **not** the Clean/Hexagonal target either, and it has *less* tenant protection than Backend (no Hibernate `tenantFilter`).
- **Conclusion:** V2 is not a migration source. It's a source of a few good ideas (§4) to port before deletion.

---

## 2. Gap analysis (severity-rated)

| ID | Sev | Gap | Evidence | Fix (phase) |
|---|---|---|---|---|
| G1 | **P0** | Identity/auth lives in the shared kernel and depends on business modules | 343 frozen `shared→module` violations, 241 from `shared.security.service` | Extract `identity` module (P2) |
| G2 | **P0** | Tier-1 wide `@NamedInterface` grants expose entities/repos of user, gym, organisation, membership | ADR-0001 §3; 26+ import sites | Public facades + read DTOs per module, then revoke grants (P4) |
| G3 | **P0** | `payment` ↔ `subscription` cycle hidden by `Type.OPEN` | payment imports `subscription.domain` ×5 | Merge into `billing` (P4) |
| G4 | **P0** | Cross-module events aren't durable or tenant-safe | no `spring-modulith-starter-jpa`, 0 `@ApplicationModuleListener`, `TenantAwareEvent` not wired | Event registry + listener conversion (P1/P5) |
| G5 | P1 | Domain isn't framework-free (63 domain classes are `@Entity`, Lombok `@Data`) | 1,186 domain-purity violations | Domain model + JPA entity + mapper per aggregate (P3/P4) |
| G6 | P1 | Application layer talks to Spring Data/web directly | 524 violations | Outbound ports (`…RepositoryPort`) + adapters (P3/P4) |
| G7 | P1 | Two inconsistent module layouts + empty `classes/` package | tree scan | One template for all modules (P1) |
| G8 | P1 | Module-owned enums in `shared.constants` | 18 files | Move to owning module's public API (P1) |
| G9 | P1 | Controllers (45) and repos (62) outside `infrastructure.{web,persistence}` | frozen rules | Mechanical moves (P4) |
| G10 | P2 | No per-module integration tests (`@ApplicationModuleTest`), Testcontainers IT skipped locally | 3 skipped | Add per module (P6) |
| G11 | P2 | `spring-ai` 1.0.0-M1 milestone dependency in a production build | pom | Pin to GA during upgrade track |
| G12 | P2 | Platform versions: Boot 3.5.6 / Modulith 1.4 vs V2's Boot 4.1 / Modulith 2.1 | poms | **Separate track after the refactor**, not mixed in |

---

## 3. Target architecture

### Module map (5 merges, 13 business modules + kernel)

| Target module | From | Notes |
|---|---|---|
| `shared` (kernel) | `shared` minus security/auth | Base types, `TenantContext`/`TenantIdentity`/`TenantAwareEvent`, error model, `ApiResponse`, permission annotations. **Depends on no module.** |
| `identity` | `user` + `shared.security.*` | Users, staff, trainers, invites, auth, JWT, password/OTP/TOTP, security filter chain |
| `onboarding` | `shared.domain.PendingRegistration` + registration flow | Idea from V2: own bounded context, initiate → verify → complete |
| `organisation` | `organisation` + `gym` | Org → Gym hierarchy is one aggregate family |
| `whitelabel` | `whitelabel` | unchanged scope |
| `membership` | `membership` | |
| `billing` | `payment` + `subscription` | Retires the `Type.OPEN` workaround |
| `scheduling` | `scheduling` | |
| `access` | `access` | Check-in; receives V2's cross-gym access rule |
| `health` | `health` | |
| `retail` | `inventory` + `pos` | POS already depends only on `inventory.api` |
| `notification` | `notification` | |
| `crm` | `lead` | |
| `ai` | `ai` | |
| `reporting` | `analytics` + `admin` | read-only consumers of other modules' facades |

### Per-module layout (matches the existing `CleanArchitectureTest`)

```
com.gymmate.<module>/
├── <Module>Api.java              # public facade interface(s): sync queries/commands for other modules
├── dto/                          # public records (@NamedInterface "dto")
├── events/                       # public event records, implement TenantAwareEvent (@NamedInterface "events")
└── internal/
    ├── domain/                   # PURE Java: aggregates, value objects, domain events, domain exceptions
    ├── application/
    │   ├── usecase/              # services/use cases (+ facade impl), @Transactional allowed
    │   └── port/                 # outbound ports: <Agg>RepositoryPort, PaymentGatewayPort, …
    └── infrastructure/
        ├── web/                  # @RestController + request/response mapping
        ├── persistence/          # <Agg>JpaEntity, Spring Data repo, <Agg>PersistenceAdapter, mapper
        ├── messaging/            # @ApplicationModuleListener consumers
        ├── integration/          # Stripe, SMTP, OpenAI adapters
        └── config/
```

### Rules

1. Other modules may only use `<Module>Api`, `dto`, `events`. No `@NamedInterface` on anything under `internal`.
2. Side effects across modules go through events (`@ApplicationModuleListener`, durable registry, `TenantAwareEvent`). The only exception is documented synchronous listeners that must share the publisher's transaction (e.g. `MembershipPaymentEventListener`, ADR-0001 §5).
3. Cross-module references are by ID only. No cross-module JPA associations.
4. Flyway tables and columns stay unchanged. This refactor is code-only, so **no data migration** is needed (the one exception is the `event_publication` table).
5. The HTTP contract stays frozen by `ApiEndpointSnapshotTest` (377 routes).

---

## 4. Backend_v2 salvage list (port, then delete)

| Port | Destination |
|---|---|
| `MemberService.canAccessGym` + `AccessDecision` (member can enter any gym in the same organisation with an ACTIVE membership) + `CrossGymAccessTest` | `membership` facade, consumed by `access` check-in. This also feeds the P0 "expired members keep access" fix |
| Onboarding state machine (`PendingRegistration` initiate → verify → complete, expiry checks) | new `onboarding` module, reconciled with the current `AuthenticationService` registration flow |
| Freeze validation (ACTIVE-only, future end date, max-days vs `FreezePolicy`) | diff against Backend's freeze flow and keep the stricter rule |
| `@Version` optimistic locking on the base entity | the JPA base entity in persistence (needs a migration adding `version` columns, so do it as a separate commit) |
| `@Modulith(systemName, sharedModules = "shared")` | `GymMateApplication` |
| Records for all public DTOs/events | convention for every module's `dto/` and `events/` |
| Boot 4.1.1 / Modulith 2.1.1 / JJWT 0.13 pins | upgrade-track notes (G12) |

Everything else in V2 duplicates Backend (or is weaker than it).

---

## 5. Execution plan

Every step ends with: `./mvnw -B test` green, `ModularityTests` green, API snapshot unchanged, ArchUnit store re-frozen (`-Darchunit.freeze.refreeze=true`) with a **smaller** count, and one commit per step.

| Phase | Work | Exit criteria | Est.* |
|---|---|---|---|
| **0** | Safety net: API snapshot, ArchUnit ratchet, Modulith verify | ✅ done (`cf89a84`) | – |
| **1 Foundations** | Add `spring-modulith-starter-jpa` + `event_publication` migration; `@Modulith`; move 18 module enums out of `shared.constants`; delete empty `classes/`; add ArchUnit rule "only `Api`/`dto`/`events` are public"; write ADR-0002 | Template agreed, registry live | 1–2 d |
| **2 Identity extraction** | Move `shared.security.*` + `user` → `identity`; `shared` keeps `TenantContext` + a `CurrentUser` abstraction; `PendingRegistration` → `onboarding` | `shared→module` = **0**; `shared` loses no `OPEN` behaviour it needs | 2–3 d |
| **3 Pilot** | `crm` (lead) as a small rehearsal, then `membership` as the real pilot: domain/JPA split, ports, adapters, mapper, facade, events | Both modules have 0 frozen violations; pattern documented | 2 d |
| **4 Roll-out** | Leaves first: ai → whitelabel → health → retail (inventory+pos) → scheduling → access (+ cross-gym rule) → notification → organisation (+gym) → billing (payment+subscription) → identity remainder → reporting (analytics+admin). Revoke each Tier-1 grant as its callers move to facades | All modules 0 violations; `subscription` no longer `OPEN`; no `@NamedInterface` under `internal` | 8–12 d (strict) / 4–6 d (pragmatic) |
| **5 Events** | Convert cross-module listeners to `@ApplicationModuleListener`; every public event implements `TenantAwareEvent`; incomplete-publication republish on startup | No plain cross-module `@EventListener` except the documented sync ones | 1–2 d |
| **6 Lock-in** | ArchUnit store empty, so remove `freeze()`; `@ApplicationModuleTest` per module; Documenter diagrams into `docs/`; README + ADR-0002 final | Rules enforced hard in CI | 1 d |
| **7 Cleanup** | Port §4 salvage; boot against Postgres via `docker compose`; delete `Backend_v2/` | V2 gone, nothing lost | 0.5 d |
| *Later* | Boot 4 / Modulith 2 upgrade; optional `com.gymmate → com.gymmatehub` rename | separate branches | – |

\* Engineer-days for one senior engineer with AI assistance. "Strict" means separate pure domain models and JPA entities with mappers for all 67 entities. "Pragmatic" means JPA annotations are tolerated on domain aggregates, with only Spring and web kept out of the domain.

### Sequencing constraint

Per the project's own rule, the P0 product fixes (payment-failure chain, tenant isolation, check-in, member portal) ship **before** the structural moves in Phases 2–4, or else land on `restructure` first. Phases 0–1 are safe to run in parallel with P0 work. Phase 2 onward touches the same files the P0 fixes touch, so expect merge conflicts if the two run concurrently.

---

## 6. Definition of done

- [ ] `ApplicationModules.verify()` passes with **no** `Type.OPEN` except `shared`, and **no** `@NamedInterface` on `domain`/`infrastructure`/`internal`
- [ ] ArchUnit store empty and rules un-frozen
- [ ] `ApiEndpointSnapshotTest` unchanged (377 routes) and the frontend/mobile contract intact
- [ ] All tests green, plus `@ApplicationModuleTest` per module and the Testcontainers ITs passing in CI
- [ ] Flyway: only additive migrations (`event_publication`, optional `version` columns)
- [ ] App boots via `docker compose` against PostgreSQL 18 + Redis
- [ ] V2 salvage items ported with tests; `Backend_v2/` deleted
- [ ] ADR-0002 records the final module map and the exceptions
