# GymMateHub – Comprehensive Gym Management SaaS Platform
## Business Requirements Document (BRD) | Version 0.1 (Pre-launch Draft)

| Field | Value |
|-------|-------|
| **Version** | 0.1 |
| **Last Updated** | June 27, 2026 |
| **Status** | Pre-launch Draft |

> **Versioning:** all GymMateHub documents share a single **v0.1** pre-launch baseline, tracked centrally in [`VERSIONS.md`](VERSIONS.md). Versions advance together; first public release will be **v1.0**.

---

## Document History

| Version | Date | Summary |
|---------|------|---------|
| 0.1 | June 27, 2026 | Consolidated pre-launch baseline. Covers MVP (membership, payments, classes, health, inventory, newsletter, multi-channel notifications) plus planned Access Control & Anti-Tailgating (§7.10), AI Personal Trainer (§7.11), Upgradeability principle (§6.4), Competitive Positioning (§20), and website reconciliation (§21). |

---

## 1. Executive Summary

GymMateHub is a cloud-based, multi-tenant Gym Management Software-as-a-Service (SaaS) platform designed to streamline operations, enhance member engagement, and provide actionable business insights for fitness businesses of all sizes. The platform addresses fragmented gym operations by unifying membership management, payments, scheduling, attendance, staff management, analytics, and integrations into a single, scalable system.

**The MVP is complete and in active use.** This document captures the full business requirements to guide post-MVP expansion, enterprise readiness, and long-term scalability.

---

## 2. Business Objectives

- Digitize and automate end-to-end gym operations
- Reduce administrative overhead for gym owners and managers
- Improve member retention and engagement
- Enable data-driven decision-making through analytics
- Provide a scalable, secure, and customizable multi-tenant platform
- Support white-labeling for enterprise and franchise gyms

---

## 3. Problem Statement

Most gyms rely on fragmented tools for payments, attendance, scheduling, and member communication. This leads to:

- Manual errors and inefficiencies
- Poor member experience
- Revenue leakage
- Limited visibility into business performance
- Difficulty scaling across multiple locations

**GymMateHub solves these problems with an integrated, cloud-native solution.**

---

## 4. Target Users & Personas

| Persona | Description | Key Needs |
|---------|-------------|-----------|
| **Gym Owners** | Single-location & multi-branch operators | Business analytics, revenue tracking, staff management |
| **Gym Managers / Admin Staff** | Day-to-day operations | Member management, scheduling, reporting |
| **Trainers / Coaches** | Class instructors and personal trainers | Schedule management, client tracking, workout logging |
| **Gym Members** | End users of the gym | Class booking, payments, fitness tracking, progress monitoring |
| **Enterprise / Franchise Operators** | Multi-location businesses | Centralized management, white-labeling, cross-location analytics |

---

## 5. Scope

### 5.1 In Scope (Implemented)

Status legend: `✅ Complete` (fully delivered), `🔄 Partial` (usable but not full parity), `📋 Planned` (not yet implemented), `⚠️ Stub/Limited` (scaffolded path).

| Feature Area | Status | Description |
|--------------|--------|-------------|
| Multi-tenant gym onboarding | ✅ Complete | Organisation and gym registration with tenant isolation |
| Membership & subscription management | ✅ Complete | Plans, enrollments, freezes, renewals |
| Payments & billing | ✅ Complete | Stripe integration, Connect, invoices, refunds |
| Class scheduling & bookings | ✅ Complete | Class management, schedules, member bookings |
| Trainer & staff management | ✅ Complete | Staff profiles, trainer assignments, permissions |
| Health & fitness tracking | ✅ Complete | Exercise library, workout logging, health metrics, goals |
| Equipment & inventory management | ✅ Complete | Equipment tracking, maintenance, stock management |
| Reporting & analytics | ✅ Complete | Health dashboard, analytics services |
| Mobile & web API access | ✅ Complete | RESTful API with OpenAPI documentation |
| Authentication & security | ✅ Complete | JWT, 2FA (TOTP), password reset, token blacklist |
| **Newsletter & Campaign Management** | ✅ Complete | **NEW** - Email templates, bulk campaigns, recipient tracking |
| **Multi-Channel Notification** | 🔄 Partial | Email complete, SMS/WhatsApp infrastructure ready (senders are stubs — see §10) |
| **Access Control & Anti-Tailgating** | 🔄 Partial | Software check-in (QR/passcode), anti-tailgating rules, pluggable device port and migration are implemented; hardware adapters/SSE stream parity still pending (Section 7.10) |
| **AI Personal Trainer** | 🔄 Partial (Prototype) | Event-driven AI plan generation and storage are implemented; full PRD entities/endpoints/guardrails are still pending (Section 7.11) |

### 5.2 Out of Scope (Future Phases)

- Hardware biometric device provisioning (note: software access core is **in scope**; physical hardware integrates via the device-adapter port)
- Blockchain/on-chain payments
- Insurance integrations
- White-label mobile apps (a Flutter/Dart member app is **roadmapped**, see §14 — white-label is later)
- Virtual training platform
- AI predictive analytics, smart scheduling, engagement scoring (later AI-module phases; the AI Personal Trainer in §7.11 **is** in scope)

---

## 6. Current Implementation Status

### 6.1 Technology Stack

| Layer | Technology | Details |
|-------|------------|---------|
| **Runtime** | Java 21 (LTS) | Long-term support version |
| **Framework** | Spring Boot 3.5.6 | Latest stable release |
| **Build** | Maven 3.x | Via Maven Wrapper (mvnw) |
| **Database** | PostgreSQL 15+ | Production database |
| **Dev Database** | H2 | In-memory for development |
| **Migrations** | Flyway | Schema versioning (10 migrations, including Access V10) |
| **Security** | Spring Security + JWT | With TOTP 2FA support |
| **API Docs** | SpringDoc OpenAPI 3.x | Swagger UI available |
| **Payments** | Stripe (with Connect) | Platform & connected accounts |
| **Code Gen** | Lombok + MapStruct | Boilerplate reduction |
| **Caching** | Redis | Session and rate limiting |
| **Containers** | Docker | Multi-stage builds |
| **CI/CD** | GitHub Actions | Automated testing & deployment |
| **Hosting** | Railway | Cloud deployment |

### 6.2 Module Architecture

The backend follows a **hexagonal/clean architecture** pattern:

```
src/main/java/com/gymmate/
├── GymMateApplication.java          # Entry point
├── shared/                          # Cross-cutting concerns
│   ├── config/                      # Configuration classes (9 files)
│   ├── security/                    # JWT, Auth, Token management (25 files)
│   ├── exception/                   # Global exception handling (9 files)
│   ├── service/                     # Shared services (Email, Password)
│   ├── multitenancy/                # Tenant context & filtering
│   └── domain/                      # Base entities
├── organisation/                    # Organisation (tenant) management
├── user/                            # User, Member, Staff, Trainer
├── gym/                             # Gym profiles and areas
├── subscription/                    # Platform subscriptions & rate limiting
├── classes/                         # Class scheduling & bookings
├── membership/                      # Membership plans & subscriptions
├── payment/                         # Stripe, webhooks, refunds
├── inventory/                       # Equipment, stock, maintenance
├── health/                          # Exercise, workouts, metrics, goals
├── notification/                    # NEW: Newsletter & multi-channel messaging
├── access/                          # PLANNED: Access control & anti-tailgating (Section 7.10)
├── ai/                              # PLANNED: AI Personal Trainer — workout + meal plans (Section 7.11)
├── analytics/                       # Reserved: Advanced/predictive analytics (scaffolded)
├── booking/                         # Reserved: Facility/PT/space booking (scaffolded — decision: build or remove claim)
└── dashboard/                       # Reserved: Dashboard features (analytics module serves this today)
```

### 6.3 Implemented API Endpoints

| Module | Controllers | Key Endpoints |
|--------|-------------|---------------|
| **Organisation** | OrganisationController | `/api/organisations/**` |
| **User** | UserController, MemberController, StaffController, TrainerController | `/api/users/**`, `/api/members/**`, `/api/staff/**`, `/api/trainers/**` |
| **Gym** | GymController | `/api/gyms/**` |
| **Auth** | AuthController | `/api/auth/**` |
| **Subscription** | SubscriptionController | `/api/subscriptions/**` |
| **Classes** | ClassController, ClassScheduleController, ClassBookingController, ClassCategoryController, GymAreaController | `/api/classes/**`, `/api/schedules/**`, `/api/bookings/**` |
| **Membership** | MembershipController, MembershipPlanController, MemberPaymentController | `/api/memberships/**`, `/api/membership-plans/**` |
| **Payment** | PaymentController, ConnectController, StripeWebhookController, GymOwnerRefundController, MemberRefundController | `/api/payments/**`, `/api/connect/**`, `/api/webhooks/**` |
| **Inventory** | EquipmentController, InventoryController, MaintenanceController, SupplierController | `/api/equipment/**`, `/api/inventory/**`, `/api/maintenance/**` |
| **Health** | ExerciseController, WorkoutController, HealthMetricController, FitnessGoalController, HealthDashboardController | `/api/exercises/**`, `/api/workouts/**`, `/api/health/**` |
| **Newsletter** | NewsletterTemplateController, NewsletterCampaignController | `/api/newsletters/**`, `/api/campaigns/**` |

**Total Controllers: 33**

### 6.4 Upgradeability & Extensibility Principle (NEW)

GymMateHub is built so capabilities can be **upgraded without rework**. Every external/variable capability sits behind a **port/adapter** so providers swap freely:

| Concern | Port | Default adapter | Future adapters |
|---------|------|-----------------|-----------------|
| Physical access | `AccessDevicePort` | `SoftwareAccessAdapter` (no hardware) | Turnstile/maglock, camera/CV |
| AI generation | `LlmClient` | Anthropic Claude | OpenAI, others |
| Payments | payment-provider abstraction | Stripe | Paystack, Flutterwave, PayPal, Square |
| Messaging | `ChannelSender` | Email | SMS (Twilio), WhatsApp, Push |

Supporting rules: **`/api/v1` versioning** on all new endpoints; domain logic stays provider-agnostic; configuration via environment variables (Stripe-config pattern); graceful degradation when an integration key is absent. This is the architectural through-line for all new modules.

---

## 7. Functional Requirements

### 7.1 Multi-Tenant Management ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Gym onboarding with tenant isolation | ✅ | `Organisation` entity with `organisation_id` on all tenant data |
| Tenant-level configuration | ✅ | Organisation settings, subscription plans, feature flags |
| Subscription plan enforcement | ✅ | `SubscriptionTier`, `Subscription`, rate limiting |
| Custom branding per tenant | ✅ | Gym-level customization (logo, settings) |
| Rate limiting per tenant | ✅ | `ApiRateLimit` with `RateLimitService` |

### 7.2 User Management & Authentication ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Role-based access control | ✅ | `UserRole` enum: OWNER, STAFF, TRAINER, MEMBER |
| Secure authentication | ✅ | JWT with Spring Security |
| Password reset | ✅ | `PasswordResetToken` entity & service |
| MFA support | ✅ | TOTP via `TotpService` |
| User profile management | ✅ | Full CRUD for User, Member, Staff, Trainer |
| Token blacklisting | ✅ | `TokenBlacklist` for logout/invalidation |

### 7.3 Member Management ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Member registration & profiles | ✅ | `Member` entity with health info, emergency contacts |
| Membership plans & renewals | ✅ | `MembershipPlan`, `MemberMembership` entities |
| Status tracking | ✅ | `MembershipStatus` enum (ACTIVE, FROZEN, EXPIRED, etc.) |
| Membership freezing | ✅ | `FreezePolicy`, freeze tracking on memberships |
| Health & emergency information | ✅ | Stored on Member entity |

### 7.4 Payments & Billing ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Subscription billing | ✅ | Stripe subscriptions with webhooks |
| Online payments | ✅ | Stripe payment intents, Connect for gym payouts |
| Invoices & receipts | ✅ | `MemberInvoice`, `GymInvoice` entities |
| Refund processing | ✅ | `RefundRequest`, `PaymentRefund` with audit log |
| Revenue tracking | ⚠️ | Via invoices (calculation methods pending) |
| Webhook handling | ✅ | `StripeWebhookService` with event deduplication |

### 7.5 Scheduling & Bookings ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Class & session scheduling | ✅ | `GymClass`, `ClassSchedule` entities |
| Trainer assignment | ✅ | `trainer_id` on ClassSchedule |
| Capacity limits | ✅ | `capacity` field with override support |
| Member bookings & cancellations | ✅ | `ClassBooking` with status tracking |
| Gym areas/rooms | ✅ | `GymArea` entity for room management |

### 7.6 Trainer & Staff Management ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Staff onboarding & roles | ✅ | `Staff` entity with position, department |
| Trainer profiles | ✅ | `Trainer` entity with specializations, certifications |
| Schedule assignments | ✅ | Trainer linked to ClassSchedule |
| Performance metrics | ✅ | Via analytics and class data |

### 7.7 Health & Fitness Tracking ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Exercise library | ✅ | `Exercise`, `ExerciseCategory` entities |
| Workout logging | ✅ | `WorkoutLog`, `WorkoutExercise` entities |
| Health metrics | ✅ | `HealthMetric` with various metric types |
| Fitness goals | ✅ | `FitnessGoal` with progress tracking |
| Progress photos | ✅ | `ProgressPhoto` entity |
| Wearable sync | ⚠️ | `WearableSync` entity exists, integration pending |

### 7.8 Equipment & Inventory ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Equipment tracking | ✅ | `Equipment` entity with status, warranty |
| Inventory management | ✅ | `InventoryItem`, `StockMovement` entities |
| Maintenance scheduling | ✅ | `MaintenanceRecord`, `MaintenanceSchedule` |
| Supplier management | ✅ | `Supplier` entity |

### 7.9 Newsletter & Campaign Management ✅ Implemented (NEW)

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Newsletter templates | ✅ | `NewsletterTemplate` entity with placeholders |
| Bulk email campaigns | ✅ | `NewsletterCampaign` with audience targeting |
| Recipient tracking | ✅ | `CampaignRecipient` with delivery status |
| Audience segmentation | ⚠️ | `AudienceType` enum (some filters pending integration) |
| Scheduled sending | ✅ | `scheduled_at` field with status tracking |
| Delivery analytics | ✅ | `delivered_count`, `failed_count` tracking |

**Supported Audience Types:**
- `ALL_MEMBERS` - All active gym members
- `ACTIVE_MEMBERS` - Members with active memberships
- `INACTIVE_MEMBERS` - Members with expired/frozen memberships
- `NEW_MEMBERS` - Recently joined members
- `CLASS_ATTENDEES` - Members enrolled in specific classes (pending integration)
- `UPCOMING_BOOKINGS` - Members with upcoming class bookings (pending integration)
- `MEMBERSHIP_PLAN` - Members on specific plans (pending integration)
- `SPECIFIC_MEMBERS` - Manually selected members (pending integration)

### 7.10 Access Control & Anti-Tailgating 🔄 Partially Implemented (NEW)

**Business need:** the pilot gym requires prevention of **tailgating** — a second person entering on one valid scan. Strategy: enforce access in **software now** (zero-hardware, works on phone/kiosk) with a clean **device-adapter port** so turnstiles/maglocks and camera/CV plug in later. This also delivers the website's "QR/manual check-in, no hardware" promise and matches GymMaster's access-control surface.

| Requirement | Status | Implementation (planned) |
|-------------|--------|--------------------------|
| Member check-in via QR / passcode | ✅ | `AccessCredential` (hashed token); `POST /api/v1/access/scan` |
| Check-in via NFC / key-fob / Bluetooth | 📋 | via device-adapter port (later tiers) |
| Entry-decision pipeline | 🔄 | implemented for credential/membership/signup/door-benefit/access-window; overdue/visits/stop-at-gate checks reserved |
| Standard denial reasons (GymMaster parity) | 🔄 | `DenyReason` enum complete; some reasons are placeholders until related data sources are added |
| Door benefits (plan → which doors) | ✅ | `DoorBenefit` mapping `MembershipPlan` ↔ `AccessPoint` |
| Per-member/plan access-time windows | ✅ | `AccessSchedule` |
| **Anti-tailgating: one-open-session / pass-back** | ✅ | member already INSIDE cannot re-enter without an OUT |
| **Anti-tailgating: re-entry lockout** | ✅ | configurable cooldown blocks instant credential reuse |
| **Anti-tailgating: device-pass reconciliation** | ✅ | device webhook supports `passCount > validScanCount` flagging |
| **Anti-tailgating: CV image capture** | 🔄 | event supports `capturedImageUrl`; camera adapter integration pending |
| Real-time staff alerts | 🔄 | domain events are published; dedicated `/api/v1/access/stream` feed not yet exposed |
| Audit trail / Visitors log + tailgating report | ✅ | append-only `AccessEvent`; `GET /api/v1/access/events/gym/{gymId}` |
| Pluggable hardware (turnstile/maglock/camera) | 🔄 | `AccessDevicePort` exists with SOFTWARE adapter; TURNSTILE/CV adapters pending |

**Entities:** `AccessPoint`, `AccessCredential`, `DoorBenefit`, `AccessSchedule`, `AccessEvent` (all gym-scoped). **Migration:** `V10__Access_Control_System.sql`.

### 7.11 AI Personal Trainer 🔄 Partially Implemented (Prototype) (NEW)

**Business need:** at onboarding, capture the member's fitness goal and basics, then generate a **personalized workout plan** (grounded in the gym's exercise library) and a **meal plan tailored to local cuisine** (derived from the gym's country/city — "Built for Africa") to help reach that goal. This is a primary **USP**: GymMaster has no nutrition/meal AI.

| Requirement | Status | Implementation (planned) |
|-------------|--------|--------------------------|
| Onboarding goal + profile capture | 🔄 | event-driven flow consumes `MemberOnboardedEvent`; structured `MemberPlanProfile` entity not yet implemented |
| Location-derived cuisine region | ✅ | AI prompt uses gym city/country context when available |
| Provider-agnostic LLM | 🔄 | generation exists through Spring AI `ChatClient`; explicit `LlmClient` abstraction not yet implemented |
| Personalized workout plan | 🔄 | AI-generated workout text is persisted; grounding to exercise IDs is pending |
| Locally-tailored meal plan | 🔄 | AI-generated local meal text is persisted; macros/schema validation pending |
| Safety guardrails | 📋 | strict JSON validation and medical/allergy guardrails not yet implemented |
| Async generation + scheduled refresh | 🔄 | async generation implemented; scheduled re-planning not yet implemented |
| Traceability | 🔄 | `AiRecommendation` persistence exists; provider/model/token audit fields pending |
| Graceful degradation | 🔄 | failures are handled gracefully; env-key based feature gating is pending |

**Entities:** `MemberPlanProfile`, `WorkoutPlan`/`WorkoutPlanDay`/`PlanExercise`, `MealPlan`/`MealPlanDay`/`PlanMeal`, `AiRecommendation`. **Migration:** `V11__AI_Trainer.sql`. **Endpoints:** `/api/v1/ai/**`.

> Predictive analytics, smart scheduling, and engagement intelligence (also advertised) are **later AI-module phases**, not part of this first cut.

---

## 8. Non-Functional Requirements

| Requirement | Target | Implementation Status |
|-------------|--------|----------------------|
| High availability | 99.9% uptime | ✅ Railway cloud hosting |
| Horizontal scalability | Auto-scaling | ✅ Containerized deployment |
| Data isolation per tenant | Complete isolation | ✅ `organisation_id` filtering |
| GDPR-aligned data handling | Compliant | ❌ Not implemented |
| Encryption (at rest) | AES-256 | ✅ Database-level |
| Encryption (in transit) | TLS 1.3 | ✅ HTTPS enforced |
| API-first architecture | RESTful | ✅ OpenAPI documented |
| Rate limiting | Per tenant | ✅ `RateLimitService` |
| Caching | Redis-based | ✅ Session and data caching |

---

## 9. Database Schema Overview

The schema is defined across multiple Flyway migrations in `src/main/resources/db/migration/`.

### 9.1 Migrations

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__Complete_Schema.sql` | Core schema (46KB) |
| V1.1 | `V1_1__Add_Missing_Columns_To_Equipment.sql` | Equipment table fixes |
| V2 | `V2__Newsletter_Tables.sql` | Newsletter templates, campaigns, recipients |
| V3 | `V3__Multi_Channel_Support.sql` | Multi-channel support columns |

### 9.2 Core Tables

| Domain | Tables |
|--------|--------|
| **Organisation** | `organisations`, `gyms`, `gym_areas` |
| **Users** | `users`, `staff`, `trainers`, `members` |
| **Subscriptions** | `subscription_tiers`, `subscriptions`, `subscription_usage`, `api_rate_limits` |
| **Classes** | `class_categories`, `classes`, `class_schedules`, `class_bookings` |
| **Membership** | `membership_plans`, `member_memberships`, `freeze_policies`, `member_invoices`, `member_payment_methods` |
| **Payments** | `payment_methods`, `gym_invoices`, `payment_refunds`, `refund_requests`, `refund_audit_log`, `stripe_webhook_events` |
| **Health** | `exercise_categories`, `exercises`, `workout_logs`, `workout_exercises`, `health_metrics`, `fitness_goals`, `progress_photos`, `wearable_syncs` |
| **Inventory** | `equipment`, `inventory_items`, `stock_movements`, `maintenance_records`, `maintenance_schedules`, `suppliers` |
| **Security** | `pending_registrations`, `password_reset_tokens`, `token_blacklist` |
| **Newsletter** | `newsletter_templates`, `newsletter_campaigns`, `campaign_recipients` |

### 9.3 Key Design Decisions

- **UUID Primary Keys**: All tables use `uuidv7()` for globally unique, time-sortable IDs
- **Soft Deletes**: `is_active` boolean flag on most entities
- **Audit Fields**: `created_at`, `updated_at`, `created_by`, `updated_by` on all entities
- **Multi-tenancy**: `organisation_id` and/or `gym_id` on tenant-scoped data
- **JSON Fields**: Flexible storage for settings, preferences, certifications

---

## 10. Integrations

### 10.1 Implemented

| Integration | Provider | Purpose | Status |
|-------------|----------|---------|--------|
| Payment Processing | **Stripe** | Subscriptions, one-time payments, Connect for gym payouts | ✅ Complete |
| Webhook Events | **Stripe Webhooks** | Real-time payment status updates | ✅ Complete |
| Email | **SMTP (Mailtrap dev)** | Transactional emails, newsletters, OTP | ✅ Complete |

### 10.2 Planned

| Integration | Provider | Purpose | Status | Priority |
|-------------|----------|---------|--------|----------|
| **SMS Notifications** | Twilio | Renewal reminders, alerts (site lead value-prop) | 🔄 Stub — implement sender | 🔴 High |
| **WhatsApp Messaging** | WhatsApp Business API / Twilio | Renewal reminders, member comms (site lead value-prop) | 🔄 Stub — implement sender | 🔴 High |
| **Local payment rails** | Paystack, Flutterwave | African card/bank/USSD/mobile-money (Stripe coverage thin in Africa) | 📋 Strategic decision | 🔴 High |
| AI / LLM | Spring AI ChatClient (current), provider-agnostic port planned | AI Personal Trainer (§7.11) | 🔄 Partial prototype | 🟡 Med |
| Access hardware | Turnstile/maglock controllers, camera/CV | Physical access via `AccessDevicePort` (§7.10) | 🔄 Port implemented; hardware adapters pending | 🟡 Med |
| Additional gateways | PayPal, Square | Advertised on site | 📋 Not started | 🟢 Low |
| Fitness Wearables | Apple Health, Google Fit, Fitbit, Garmin | Activity data sync | ⏳ Schema only | 🟢 Low |
| Fitness apps | MyFitnessPal, Strava, Peloton | Advertised on site | ⏳ Not started | 🟢 Low |
| Workflow/automation | Zapier (webhooks) | Integration breadth | ⏳ Not started | 🟢 Low |
| Accounting | QuickBooks, Xero | Financial reconciliation | ⏳ Not started | 🟢 Low |

### 10.3 Multi-Channel Notification Infrastructure (NEW)

The `notification` module provides a channel-agnostic broadcasting system:

```
notification/
├── api/                    # REST controllers
├── application/
│   ├── BroadcastService    # Multi-channel orchestrator
│   ├── AudienceResolver    # Member filtering by audience type
│   ├── NewsletterCampaignService
│   ├── NewsletterTemplateService
│   └── channel/
│       ├── ChannelSender (interface)
│       ├── EmailChannelSender     ✅ Implemented
│       ├── SmsChannelSender       ⚠️ Stub (Twilio pending)
│       └── WhatsAppChannelSender  ⚠️ Stub (API pending)
├── domain/
│   ├── NewsletterTemplate
│   ├── NewsletterCampaign
│   ├── CampaignRecipient
│   ├── NotificationChannel (EMAIL, SMS, WHATSAPP, PUSH)
│   └── NotificationSettings
└── infrastructure/         # Repository implementations
```

**Fallback Behavior**: If preferred channel fails, system automatically falls back to email.

---

## 11. Assumptions & Constraints

### Assumptions
- Users have reliable internet access
- SaaS subscription-based revenue model
- Cloud-hosted infrastructure (Railway, with option for AWS/Azure)
- Gyms have basic digital literacy

### Constraints
- Java 21 required for compilation
- PostgreSQL 15+ for production
- Stripe account required for payments
- Environment variables required (`.env` file)
- H2 database for tests (no `uuidv7()` support - Flyway disabled)

---

## 12. Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Payment gateway downtime | Low | High | Webhook retry logic, monitoring |
| Tenant data isolation failure | Low | Critical | Strict `organisation_id` filtering, security audits |
| Regulatory changes (GDPR, etc.) | Medium | Medium | Privacy-by-design, data retention policies (pending) |
| Scalability bottlenecks | Medium | High | Load testing, horizontal scaling |
| Key personnel departure | Medium | Medium | Documentation, knowledge sharing |
| Low test coverage | Medium | High | Prioritize critical path testing |

---

## 13. Success Metrics

| Metric | Description | Target |
|--------|-------------|--------|
| **Monthly Active Gyms (MAG)** | Gyms with active subscriptions | 50+ by Q2 2026 |
| **Member Retention Rate** | % members renewing | >80% |
| **Churn Rate** | Monthly gym cancellations | <5% |
| **Average Revenue Per Gym (ARPG)** | Monthly revenue per gym | $200+ |
| **System Uptime** | Platform availability | 99.9% |
| **API Response Time** | P95 latency | <500ms |

---

## 14. Roadmap

| Phase | Timeline | Focus | Status |
|-------|----------|-------|--------|
| **Phase 1** | Q1-Q2 2025 | MVP Development | ✅ Complete |
| **Phase 2** | Q3-Q4 2025 | Feature Expansion (Health, Inventory) | ✅ Complete |
| **Phase 2.5** | Q1 2026 | Newsletter & Multi-Channel Notifications | ✅ Complete |
| **Phase 3** | Q2-Q3 2026 | **Access Control & Anti-Tailgating** (§7.10) | 🔄 In Progress |
| **Phase 3.5** | Q3 2026 | **AI Personal Trainer** — workout + locally-tailored meal plans (§7.11) | 📋 Planned |
| **Phase 3.6** | Q3 2026 | **WhatsApp + SMS reminders** (make site headline real) + local payment rails decision | 📋 Planned |
| **Phase 3.7** | Q4 2026 | GDPR compliance, service-test backfill, foundations (`/api/v1`, Testcontainers) | 📋 Planned |
| **Phase 4** | Q4 2026 – Q1 2027 | **Flutter/Dart member mobile app** (QR check-in, AI plans, push, wearables) | 📋 Planned |
| **Phase 4.5** | Q1-Q2 2027 | Predictive analytics & AI (churn/revenue forecast, smart scheduling, engagement) | 📋 Planned |
| **Phase 5** | 2027 | Enterprise/white-label, Marketplace & Ecosystem | 📋 Planned |

> **Sequencing note:** Access Control is prioritised first (pilot-gym request). Foundations (`/api/v1` versioning, shared HTTP client, Testcontainers Postgres harness) are built alongside Phase 3 so new modules ship tested against the real DB.

---

## 15. Build & Run Instructions

### Prerequisites
- Java 21
- Maven 3.x (or use Maven Wrapper)
- PostgreSQL 15+ (or H2 for dev)
- Redis (for caching)
- `.env` file with required variables

### Commands

```bash
# Set Java 21 (if not default)
export JAVA_HOME=/path/to/java-21

# Build (skip tests)
./mvnw clean package -DskipTests

# Run tests
./mvnw test -Dspring.profiles.active=test

# Run application
./mvnw spring-boot:run

# Docker build
docker build -t gymmatehub-backend .
```

### Key Endpoints
- **Health Check**: `/actuator/health`
- **API Docs**: `/swagger-ui.html`
- **Auth**: `/api/auth/login`, `/api/auth/register`

---

## 16. CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment:

```yaml
Workflow: main.yml
Triggers: push/PR to main, dev branches

Jobs:
1. build
   - Checkout, setup JDK 21
   - Build with Maven (skip tests)
   - Run tests with H2 (Flyway disabled)

2. docker (on push to main/dev)
   - Build multi-stage Docker image
   - Push to Docker Hub with branch tag

3. deploy-to-railway (on push to main/dev)
   - Deploy to Railway cloud platform
```

---

## 17. Implementation Gaps & Technical Debt

### 17.1 Known TODOs (20 items)

| Priority | Area | Description | Files |
|----------|------|-------------|-------|
| 🔴 High | Payments | Revenue calculation methods not implemented | `GymService.java` |
| 🔴 High | Webhooks | Member payment failure notification missing | `StripeWebhookService.java` |
| 🔴 High | Webhooks | Stripe Connect deauthorization handling missing | `StripeWebhookService.java` |
| 🟡 Medium | Subscriptions | Event notifications not sent | `SubscriptionService.java` |
| 🟡 Medium | Newsletter | Audience filters not fully integrated | `AudienceResolver.java` |
| 🟡 Medium | Payments | Email recipient should be org owner | `PaymentNotificationService.java` |
| 🟢 Low | SMS | Twilio integration pending | `SmsChannelSender.java` |
| 🟢 Low | WhatsApp | Business API integration pending | `WhatsAppChannelSender.java` |

### 17.2 GDPR Compliance (Not Implemented)

Required implementations:
- [ ] Data export endpoints (right to access)
- [ ] Data deletion endpoints (right to erasure)
- [ ] Consent management
- [ ] Data retention policies
- [ ] Audit logging for PII access

### 17.3 Empty Module Scaffolds

| Module | Status |
|--------|--------|
| `access/` | **Now in active development** — Access Control & Anti-Tailgating (§7.10), migration V10 |
| `ai/` | **Now in active development** — AI Personal Trainer (§7.11), migration V11 |
| `analytics/` | Reserved — predictive analytics (Phase 4.5); current analytics served by implemented `analytics` service |
| `dashboard/` | Reserved — analytics module serves this today |
| `booking/` | Reserved — facility/PT/space booking (advertised on site & by GymMaster); decision: build or drop claim |

### 17.4 Current Gap Analysis (NEW)

**Critical / production-blocking:**

| # | Gap | Notes |
|---|-----|-------|
| C1 | No real-DB integration tests | Testcontainers declared but unused; H2 hides Postgres array/jsonb/`uuidv7()` issues |
| C2 | H2 ↔ PostgreSQL drift | `text[]`/jsonb columns; new access/AI tables must use PG-safe types in test paths |
| C3 | No API versioning | add `/api/v1` before mobile/public clients; new modules ship versioned |
| C4 | GDPR not implemented | export/erasure/consent/retention/PII-audit — needed since AI stores health & diet PII |
| C5 | Thin service test coverage | User/Gym/Health/Inventory/Subscription/Security ≈ 0% |
| C6 | Open TODOs | `GymService` revenue calc; `StripeWebhookService` member-payment-failure + Connect deauthorization |

**Website (gymmatehub.com) advertised but not yet delivered:** access control / QR check-in, AI workout + nutrition, **functional WhatsApp/SMS reminders** (currently stubs — site headline), predictive analytics, member mobile app, wearable & fitness-app integrations, PayPal/Square, payroll, equipment auto-reorder. Full matrix in **Section 21**.

**Competitive (vs GymMaster) table-stakes to match:** 24/7 access control, multiple check-in methods, **tailgating detection**, space/PT booking, functional SMS/WhatsApp, more billing providers, integration breadth (Zapier/EGym/ClassPass). See **Section 20**.

---

## 18. Test Coverage

### 18.1 Current State

> **Reconciliation note:** the `PRODUCT_STATE_REPORT.md` audit (March 2026) counts **43 test classes** passing in CI — more than the figures below, which predate it. Numbers here should be re-verified against the codebase before publication. Regardless of the exact count, **service-level coverage remains thin** (security, payment, user services are the priority gaps — see §18.3).

| Metric | Value (to re-verify) |
|--------|-------|
| Total Test Files | 24 (audit reports 43 test classes) |
| Total Services | 42 |
| Service Tests | 5 (12%) |

### 18.2 Test Coverage by Module

| Module | Services | Tests | Coverage |
|--------|----------|-------|----------|
| Classes | 5 | 1 | 20% |
| Gym | 1 | 0 | 0% |
| Health | 5 | 0 | 0% |
| Inventory | 4 | 0 | 0% |
| Membership | 3 | 1 | 33% |
| Notification | 3 | 2 | 67% |
| Organisation | 2 | 0 | 0% |
| Payment | 5 | 1 | 20% |
| Shared/Security | 8 | 0 | 0% |
| Subscription | 2 | 0 | 0% |
| User | 4 | 0 | 0% |

### 18.3 Critical Test Gaps

- ❌ Authentication & Security services (security-critical)
- ❌ Payment services (financial-critical)
- ❌ User management services (core functionality)

---

## 19. Approval

This document serves as the authoritative business reference for GymMateHub development and scaling.

| Role | Name | Date |
|------|------|------|
| Product Owner | _______________ | _______________ |
| Technical Lead | _______________ | _______________ |
| Business Stakeholder | _______________ | _______________ |

---

## 20. Competitive Positioning (NEW)

Primary competitor: **GymMaster** (gymmaster.com — 110+ countries, ~180k weekly active users). Strategy: **match table-stakes, win on AI + Africa localization.**

### 20.1 Parity matrix

| GymMaster capability | GymMateHub status | Action |
|---|---|---|
| Member mgmt / paperless signup | ✅ | parity |
| Integrated/automated billing, failed-billing, refunds, credit, maintenance fees | 🟡 | Stripe core ✅; add dunning + member-credit/account ledger (C6) |
| Billing providers (Ezidebit/Paysafe/Square) | 🟡 | Stripe only → provider abstraction + local rails |
| POS + inventory | ✅ | parity |
| Booking: classes / PT / spaces, repeat/reschedule | 🟡 | classes ✅; space/PT/service booking = empty `booking/` |
| 24/7 access control + hardware | ❌ | §7.10 (software core + device port) |
| Check-in: QR / Bluetooth / passcode / key-fob / contactless | ❌ | §7.10 (QR+passcode first; rest via port) |
| **Tailgating detection** | ❌ | §7.10 (software rules now; CV camera adapter for count + image) |
| Standard denial reasons, door benefits, access-time windows, entry alerts | ❌ | §7.10 |
| Branded member app (iOS/Android) + portal | ❌ | Flutter app roadmapped (Phase 4); portal = web v1 |
| Marketing automation, Twilio SMS, Mailchimp | 🟡 | email/newsletter ✅; SMS/WhatsApp stubs → implement |
| Lead management / sales funnel | 🟡 | frontend has it; backend thin → verify/build |
| Reporting: custom, scheduled auto-send, debt-collection, visits | 🟡 | analytics strong; add scheduled send + debt/visit reports |
| Integrations: Zapier/EGym/MyWellness/Fitmetrix/ClassPass/Zoom/GA | ❌ | roadmap; Zapier/webhooks first |

### 20.2 GymMateHub USPs (GymMaster does NOT have these)

1. **AI Personal Trainer** — goal-based workout + **locally-tailored (African-cuisine) meal plans** at onboarding (§7.11).
2. **AI predictive analytics** — churn/revenue forecasting, smart scheduling, engagement intelligence (later phase).
3. **Africa-first engagement** — WhatsApp renewal reminders + cash/bank-transfer-first ledger (vs Western card/direct-debit rails).
4. **No-hardware access + optional hardware** — software tailgating defense at zero capex (vs GymMaster's mandatory Gatekeeper), clean port to add turnstile/camera later.
5. **Local payment rails** — Paystack/Flutterwave for African markets where Stripe/Ezidebit are weak.

---

## 21. Website (gymmatehub.com) — Advertised vs Delivered (NEW)

The live marketing site sells features ahead of the product. Every advertised item must be backed by a real service. (✅ built · 🟡 partial · ❌ missing/stub)

| Advertised | Status | Note |
|---|---|---|
| Multi-location mgmt, progress tracking, class scheduling, recurring billing, POS, email campaigns | ✅ | delivered |
| Cash/bank-transfer tracking (landing headline) | 🟡 | POS covers cash; explicit "who owes me" ledger thin |
| Equipment auto-reorder | 🟡 | inventory ✅; auto-reorder ❌ |
| Staff payroll integration | ❌ | advertised, not built |
| Financial reporting | 🟡 | analytics ✅; revenue-calc TODO (C6) |
| Facility/PT booking | ❌ | empty `booking/` |
| **Access control + QR/manual check-in "no hardware"** (headline) | ❌ | §7.10 delivers this |
| **WhatsApp + SMS renewal reminders** (headline) | ❌ stub | high priority — implement senders |
| **AI workout plans + nutrition guidance** | ❌ | §7.11 |
| AI predictive analytics / smart scheduling / engagement | ❌ | later AI phase |
| Member mobile app ("branded mobile & web") | ❌ | `Mobile/` empty → Flutter (Phase 4) |
| Wearables (Fitbit/Apple Health/Google Fit/Garmin) | ❌ | entity only |
| Fitness-app links (MyFitnessPal/Strava/Peloton) | ❌ | none |
| Payment gateways: Stripe / PayPal / Square | 🟡 | Stripe only |
| **Africa payment reality** | ⚠️ | Stripe coverage thin in Africa → Paystack/Flutterwave decision |
| SendGrid / Twilio / Mailchimp | 🟡 | generic SMTP ✅; Twilio/Mailchimp ❌ |

---

**Document Version**: 0.1
**Last Updated**: June 27, 2026
**Classification**: Internal Use Only
