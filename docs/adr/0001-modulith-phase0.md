# ADR 0001: Spring Modulith Phase 0 — boundary enforcement, without a big refactor

**Status:** Accepted
**Date:** 2026-08-07

## Context

The 16 top-level packages under `com.gymmate` already followed a package-by-feature
layout (`api`/`application`/`domain`/`infrastructure` per module) before this work —
migration-ready, but with no declared module boundaries and no enforcement. This ADR
records what Phase 0 did to get `ApplicationModules.verify()` passing in CI
(`src/test/java/com/gymmate/ModularityTests.java`), and the debt it deliberately
accepted to avoid a larger refactor in this pass.

Explicitly out of scope for Phase 0: schema-per-module DB split, transactional
outbox / Modulith event publication registry, `@ApplicationModuleListener`
conversion, ArchUnit, Claude LLM provider swap.

## Decisions

### 1. All 16 packages declared as `@ApplicationModule`

Zero-cost given the boundaries already existed. `admin`/`analytics` got first-class
status too, despite being thin today, so their coupling is tracked from day one rather
than growing unchecked inside an ambiguous bucket.

### 2. `shared` declared `Type.OPEN`

Cross-cutting infrastructure (multitenancy, security, base entity types, config,
exceptions) used by nearly every module. `Type.OPEN` exempts it from both boundary
verification (any module may reach into it) **and cycle detection** — a fact this
Phase 0 pass also used deliberately, see decision 6.

Review-discipline note (not a mechanism `Type.OPEN` enforces): `shared.security.service.*`
/`repository.*` should still only be touched via intended entry points
(`AuthController`, the exposed `PasswordEncoder`/`AuthenticationManager` beans), not
instantiated directly from other modules.

### 3. Wide `@NamedInterface` grants — Tier 1 documented debt

The baseline `verify()` scan found `user`, `gym`, `organisation`, `membership` function
as de-facto shared reference data — entities and repositories injected directly from
13+ other modules. Rather than a premature big-bang facade/ID-reference refactor, their
`domain`, `infrastructure`, and (where already used as a facade — `user.application`,
`gym.application`, `organisation.application`, `payment.application`,
`notification.application`) `application` sub-packages were granted `@NamedInterface`
wholesale. Same treatment extended to `classes.domain`/`infrastructure` (reached by
analytics/notification for reporting) and a handful of `api.dto` packages
(`user.api.dto`, `organisation.api.dto`, `gym.api.dto` — REST DTOs crossing module
lines, mostly via `shared.security.api.AuthController` which hosts user-registration
endpoints).

**This is real Phase 1+ work, not resolved here:** replace direct entity/repository
coupling with ID-based references or explicit read-model DTOs, and narrow these grants
down. `payment.application`/`notification.application`/`user.application`/
`gym.application`/`organisation.application`/`subscription.application` being exposed
as facades is the one part of this tier that's *not* a compromise — calling a service
rather than a repository is the architecturally-correct pattern already in use; widening
those endorses existing good practice.

### 4. Hexagonal ports added where a module boundary was closing a real cycle

Seven small ports were introduced during Phase 0, all following the same
`<module>.application.port.<Name>` interface + `<other-module>.infrastructure.<Name>Adapter`
placement `access.application.port.AccessDevicePort` already established:

| Port | Owned by | Implemented by | Replaces |
|---|---|---|---|
| `MembershipRevenueSource` | gym | membership | `GymService` reading `MemberInvoiceRepository` directly |
| `PlatformInvoiceRevenueSource` | gym | payment | `GymService` reading `GymInvoiceRepository` directly |
| `GymDirectory` | user | gym | `InviteService` calling `GymService` directly |
| `GymAccessVerifier` | notification | **shared** (`shared.integration`, not `gym`) | `NotificationController` reading `GymRepository` directly |
| `MemberLimitGuard` | user | organisation | `MemberController` calling `OrganisationLimitService` directly |
| `OrganisationSettingsSource` | notification | organisation | `BroadcastService` reading `OrganisationRepository` directly |
| `OrganisationBillingInfoProvider` | payment | organisation | `StripePaymentService`/`PaymentNotificationService`/`SubscriptionService` reading `OrganisationRepository` directly |
| `AudienceMemberIdsResolver` / `MemberDirectory` | notification | classes, membership, user | `AudienceResolver` querying 3 other modules' repositories directly |

`GymAccessVerifier`'s adapter living in `shared.integration` rather than `gym` is the
one deliberate exception to the pattern: putting it in `gym` would have made `gym`
depend on `notification`, and `notification` separately depends on `organisation` (via
`OrganisationSettingsSource`'s consumer), which already correctly depends on `gym` — a
3-module cycle. `shared` being `Type.OPEN` (decision 2) made it the natural escape
valve, matching the precedent `shared.security.service.AuthenticationService` already
set by reaching into `user` directly.

### 5. `membership↔payment` cycle closed via events, not a port

Unlike the reads above, `payment.application.StripeWebhookService`'s
`handleConnectPaymentSucceeded`/`handleConnectPaymentFailed` were **writing** directly
to `MemberMembershipJpaRepository`/`MemberInvoiceRepository` as a side effect of
processing a Stripe webhook — while `membership.application.MemberPaymentService`
separately (and correctly) depends on `payment.application.StripeConnectService` to
initiate charges. Both directions were real, so no single edge could be inverted away.

Fixed by moving the membership-state-writing logic into
`membership.application.MembershipPaymentEventListener`, listening to
`notification.events.PaymentSuccessEvent`/`PaymentFailedEvent` (extended with a
nullable `membershipId` field). Deliberately a plain `@EventListener`, **not**
`@Async`: the original code ran synchronously inside the webhook's transaction, so a
failure there failed the whole webhook and let Stripe redeliver
(`StripeWebhookService#processConnectWebhook`). A plain (non-async)
`ApplicationEventPublisher.publishEvent` call is synchronous and propagates exceptions
back to the publisher, preserving that behavior exactly.

### 6. `payment↔subscription` — declared `Type.OPEN`, not decoupled

The deepest remaining coupling: `StripeWebhookService`'s entire platform-event handling
(subscription status sync, trial tracking, invoice reconciliation — ~6 methods) exists
specifically to keep `Subscription` state current with Stripe, and
`StripePaymentService` reads `Subscription` to manage Stripe customer/subscription IDs.
Subscription state genuinely **is** payment's core write target here, not a misplaced
dependency — the module boundary between `payment` and `subscription` may itself be the
wrong cut, not something to force through events in this pass.

Spring Modulith has no primitive for "these two top-level packages are one logical
module" short of physically colocating them (a real refactor) or a custom module
detection strategy. `subscription` was declared `Type.OPEN` (same mechanism as
`shared`) to get the same practical effect — `payment`↔`subscription` stop failing
`verify()` — without moving files. **Revisit if/when payment's webhook handling is
decoupled from direct `Subscription` writes**, using the
`MembershipPaymentEventListener` pattern (decision 5) as the template.

## Addendum (2026-08-08): first Tier-1 grant narrowing landed

`user.application.port.UserQueryPort` / `user.infrastructure.UserQueryAdapter` now exist
(`findOwnerSummaryById`, `countMembersByGymId`, `countActiveMembersByGymId`,
`countUsersByOrganisationAndRole`), and `gym.application.GymService` was rewritten to
depend on it instead of injecting `user.infrastructure.UserRepository`/`MemberRepository`
directly. This is the first concrete instance of decision 3's "Phase 1+ should replace
direct entity/repository coupling ... and narrow these grants down" — recorded here, not
re-litigated. `user.infrastructure`'s wide `@NamedInterface` grant stays in place for now
(18 other call sites outside `gym` still inject those repositories directly per the Phase
0 baseline scan) — narrow it further only once enough of those callers have their own
query port.

Also landed alongside it: `shared.multitenancy.TenantIdentity` (immutable
`organisationId`/`gymId`/`userId` record) and `shared.multitenancy.TenantAwareEvent`
(marker interface for `getTenantIdentity()`), plus a `TenantScope.activate(TenantIdentity)`
overload. These aren't wired into any event class yet — see the Phase 1 plan's Stage 2
(`TenantAwareEvent` retrofit across `PaymentFailedEvent`, `PaymentSuccessEvent`, and the
other ~10 existing cross-module events) for where this goes next.

## Addendum (2026-08-08, part 2): `inventory`/`pos` Tier-1 debt retired

As part of adopting the `.api`/`.internal` functional package hierarchy (Phase 1,
Stage 3), `inventory` and `pos` were physically restructured and their wide Tier-1
`@NamedInterface` grants on `application`/`domain`/`infrastructure` (this ADR's
decision 3) were deleted outright rather than carried forward as `internal.*`. Their
two real external callers now go through proper facades instead:

- `inventory.api.InventoryFacade` (impl `inventory.internal.service.InventoryFacadeImpl`) —
  `countLowStockItems`, `recordSale`, `getItemSummary` (returns a 4-field
  `InventoryItemSummary` record, never the JPA entity). Replaces `analytics`' direct
  `InventoryItemJpaRepository` read and `pos`' direct `InventoryService`/`InventoryItem`
  use.
- `pos.api.PosFacade` (impl `pos.internal.service.PosFacadeImpl`) —
  `sumRevenueByGymIdAndDateRange`, `countTransactionsByGymIdAndDateRange` (new COUNT
  query on `SaleJpaRepository`, replacing a full-row fetch + `.size()`). Replaces
  `analytics`' direct `SaleJpaRepository` reads.

This is the second Tier-1 grant (after `UserQueryPort` for `user.infrastructure`,
first addendum above) fully retired rather than just narrowed — `inventory`/`pos` now
have zero wide grants, only the two facade interfaces as their public surface.

## Addendum (2026-08-08, part 3): `classes` `.api`/`.internal` conversion; `gym`/`user` scope narrowed to controllers only

`classes` (45 files) fully converted to the `.api`/`.internal` split, same pattern as
`inventory`/`pos`: `classes.api.ClassesFacade` (impl `classes.internal.service.ClassesFacadeImpl`)
now fronts 7 read-only aggregate queries — `analytics` was the only real external caller
(3 JPA repos, 9 call sites), replaced with the facade. `classes.domain`/`infrastructure`'s
Tier-1 `@NamedInterface` grants (decision 3) are retired, third module (after `user`'s
`UserQueryPort` and `inventory`/`pos`) to go from wide grant to a proper facade.

`gym` and `user`, by contrast, turned out to be a poor fit for a full conversion in this
pass. A fresh survey found 11 files reaching into `gym.domain`/`gym.infrastructure` and
15+ into `user.domain`/`user.infrastructure` directly, plus `gym.application.GymService`/
`user.application.*Service` already serving as legitimate facades for several of those
same callers — exactly the wide, real, Tier-1 debt decision 3 already documented, not
something a one-pass move can honestly convert to `.internal` (moving them un-narrowed
would either break 26+ call sites or put a `@NamedInterface` on a package named
`internal`, which defeats the point of the convention). Narrowing has to land first — this
is Phase 1+ work still ahead, not resolved here.

Scope for this pass was narrowed accordingly: only `gym.api.GymController` and `user.api`'s
five controllers (`InviteController`, `MemberController`, `StaffController`,
`TrainerController`, `UserController`) moved to `internal.web` — the one part of each
module with zero real external importers. `application`, `domain`, `infrastructure`,
`application/port` stay exactly where they are for both modules. `user.api`'s
`@NamedInterface` grant (originally covering the controllers too) is retired now that only
`user.api.dto` remains there — `shared.security.api.AuthController`'s actual dependency
was always on the DTOs, never the controllers themselves.

Expect the same pattern — controllers-only move, everything else deferred — when Stage 3
reaches `membership`, `organisation`, `notification`: all three are Tier-1 wide-grant
modules by the same decision 3 baseline scan.

## Addendum (2026-08-08, part 4): `membership`/`organisation`/`notification` controllers-only move

Confirmed the pattern predicted at the end of part 3: all three are Tier-1 wide-grant
modules (decision 3) with zero real external importers of their controllers, so each got
the same narrow treatment as `gym`/`user` — only the `.api` controllers moved to
`internal.web`, `application`/`domain`/`infrastructure` (+ `notification.events`,
`notification.application.port`) left untouched:

- `membership`: `MemberPaymentController`, `MembershipController`, `MembershipPlanController`
  → `internal.web`. No `membership.api` `@NamedInterface` existed to retire (only `domain`/
  `infrastructure` were granted) — it was never needed since nothing outside the module ever
  imported the controllers directly.
- `organisation`: `OrganisationController` → `internal.web`. Same story — no `api`-level
  grant existed, `application`/`domain`/`infrastructure`/`api.dto` grants untouched.
- `notification`: `NewsletterCampaignController`, `NewsletterTemplateController`,
  `NotificationController`, `NotificationStreamController` → `internal.web`. Same story —
  `application`/`domain`/`infrastructure`/`events`/`application.port` grants untouched.

One mirrored controller test moved per module where one existed:
`OrganisationControllerTest`, `NotificationControllerTest` (membership had none).

Remaining Stage 3 work: `payment`/`subscription` — deliberately last, still `Type.OPEN`
(decision 6), most complex, folded into Stage 4's merge rather than converted separately.

## Consequences

- `ModularityTests` (`ApplicationModules.verify()`) runs in the existing
  `./mvnw -B test` CI step — any new illegal cross-module reach-in fails the build.
- `access`, `admin`, `ai`, `analytics`, `health`, `inventory`, `pos`, `classes`, `gym`,
  `user`, `membership`, `organisation`, `notification`, `payment` are independently
  verified modules. `shared` and `subscription` are `Type.OPEN` (not independently
  cycle-checked).
- Tier 1 wide `@NamedInterface` grants (decision 3) and the `subscription` `OPEN`
  declaration (decision 6) are documented debt — Phase 1+ narrows them as the modules
  they cover get real decoupling work.
