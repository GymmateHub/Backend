# AGENTS.md

## Quick start (do this first)
- Use Java 21 only (`pom.xml` sets `<release>21</release>`); on macOS: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.
- Use Maven Wrapper, not system Maven: `./mvnw ...`.
- Create env file before running app: `cp .env.example .env` (`GymMateApplication` loads `.env` into system properties at startup).
- Fast sanity build: `./mvnw clean package -DskipTests`.

## Big picture architecture
- This is a modular monolith under `src/main/java/com/gymmate` (e.g., `user`, `gym`, `membership`, `payment`, `notification`, `ai`, `access`, `pos`).
- Most modules follow `api -> application -> domain -> infrastructure`; example chain: `gym/api/GymController` -> `gym/application/GymService` -> `gym/infrastructure/GymRepository` + `GymRepositoryAdapter` + `GymJpaRepository`.
- API responses are consistently wrapped with `ApiResponse<T>` (`shared/dto/ApiResponse.java`); preserve this contract in new endpoints.
- Domain errors are expected to become `ApiResponse.error(...)` via `shared/exception/GlobalExceptionHandler`.

## Multi-tenant model (critical)
- Tenant context is thread-local: `TenantContext` stores `organisationId` and optional `gymId`.
- Request flow is JWT first, tenant second: `JwtAuthenticationFilter` authenticates and sets org context, then `TenantFilter` enforces tenant presence for non-public paths (`SecurityConfig` filter order).
- Hibernate filtering is automatic via AOP: `TenantFilterAspect` enables `tenantFilter`/`gymFilter` before repository methods.
- New tenant-scoped entities should extend `TenantEntity`; gym-scoped entities should extend `GymScopedEntity`.
- If you add public endpoints, update all 3 places together: `SecurityConfig` permit list, `JwtAuthenticationFilter.shouldNotFilter`, and `TenantFilter.NON_TENANT_ENDPOINTS`.

## Security and auth conventions
- Method-level authorization (`@PreAuthorize`) is widely used in controllers even with global security rules; follow existing role style.
- Role names in use include both SaaS-level and legacy variants (`SUPER_ADMIN`, `GYM_OWNER`, `OWNER`, `ADMIN`, `MANAGER`, etc.); match existing endpoint patterns before introducing new role checks.

## Data and migration conventions
- Flyway SQL lives in `src/main/resources/db/migration` (`V1__...` through `V11__...` currently).
- Entities rely on DB-side UUID generation (`uuidv7()` in `shared/domain/BaseEntity`); tests needing Postgres often use a uuidv7 shim (`src/test/resources/db/testcontainers/uuidv7.sql`).
- `application.yml` defaults to Postgres settings but many local/test flows use H2; check profile-specific files before changing DDL assumptions.

## Integrations and cross-module flows
- Stripe has two webhook lanes: platform and connect (`payment/api/StripeWebhookController` + `payment/application/StripeWebhookService`), with idempotency via `StripeWebhookEventRepository`.
- Notifications are event-driven + SSE (`notification/events/*`, `notification/application/*`, `notification/api/NotificationStreamController`).
- AI plans use Spring AI + Redis cache + DB fallback (`ai/application/AiPlanService`, key pattern `ai:plan:{memberId}`).

## Testing and developer workflow
- Tests exist (unit + integration + Testcontainers), despite older docs saying otherwise; inspect `src/test/java/com/gymmate` for patterns.
- Common runs:
  - all tests: `./mvnw test -Dspring.profiles.active=test`
  - single test: `./mvnw -Dtest=GymControllerGetMyGymsTest test`
- CI workflow (`.github/workflows/main.yml`) is the source of truth for pipeline behavior: Java 21, package with `-DskipTests`, then test with test profile.

## Practical agent rules for this repo
- Prefer changing the smallest layer that solves the issue; avoid bypassing application services from controllers.
- Keep tenant/org consistency explicit (`organisationId`/`gymId`) when creating or querying data.
- Reuse existing repositories/adapters and event patterns before adding new infrastructure abstractions.
- Shell helpers (`build.sh`, `run.sh`) are machine-specific; prefer Maven Wrapper commands in docs and automation.

