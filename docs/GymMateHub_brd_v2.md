# GymMateHub – Comprehensive Gym Management SaaS Platform
## Business Requirements Document (BRD) v2.1

| Field | Value |
|-------|-------|
| **Version** | 2.1 |
| **Last Updated** | January 31, 2026 |
| **Status** | MVP Complete – Active Development |
| **Previous Version** | 2.0 (January 13, 2026) |

---

## Changelog (v2.1)

| Change | Description |
|--------|-------------|
| **Added** | Newsletter & Campaign Management feature (Section 7.9) |
| **Added** | Multi-Channel Notification infrastructure (Section 10.3) |
| **Updated** | Module Architecture to include new modules |
| **Updated** | Database Schema with new migrations (V2, V3) |
| **Added** | Implementation Gaps & Technical Debt section (Section 17) |
| **Updated** | Test Coverage Analysis (Section 18) |

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
| **Multi-Channel Notification** | 🔄 Partial | **NEW** - Email complete, SMS/WhatsApp infrastructure ready |

### 5.2 Out of Scope (Future Phases)

- Hardware biometric device provisioning
- Blockchain/on-chain payments
- Insurance integrations
- White-label mobile apps
- Advanced AI/ML recommendations
- Virtual training platform

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
| **Migrations** | Flyway | Schema versioning (4 migrations) |
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
├── access/                          # Reserved: Access control (scaffolded)
├── ai/                              # Reserved: AI/ML features (scaffolded)
├── analytics/                       # Reserved: Advanced analytics (scaffolded)
├── booking/                         # Reserved: General booking (scaffolded)
└── dashboard/                       # Reserved: Dashboard features (scaffolded)
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

| Integration | Provider | Purpose | Status |
|-------------|----------|---------|--------|
| SMS Notifications | Twilio | Booking reminders, alerts | 🔄 Infrastructure ready |
| WhatsApp Messaging | WhatsApp Business API | Member communication | 🔄 Infrastructure ready |
| Fitness Wearables | Apple Health, Google Fit, Fitbit | Activity data sync | ⏳ Schema only |
| Accounting | QuickBooks, Xero | Financial reconciliation | ⏳ Not started |

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
| **Phase 3** | Q1-Q2 2026 | Enterprise & White-label | 🔄 In Progress |
| **Phase 4** | Q3-Q4 2026 | Advanced Analytics & AI | 📋 Planned |
| **Phase 5** | 2027 | Marketplace & Ecosystem | 📋 Planned |

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

The following modules have directory structures but no implementation:
- `ai/` - Reserved for Phase 4 AI/ML features
- `analytics/` - Reserved for Phase 4 Advanced Analytics
- `dashboard/` - Reserved for dashboard aggregation
- `booking/` - Reserved for general booking beyond classes
- `access/` - Reserved for biometric/access control

---

## 18. Test Coverage

### 18.1 Current State

| Metric | Value |
|--------|-------|
| Total Test Files | 24 |
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

**Document Version**: 2.1  
**Last Updated**: January 31, 2026  
**Classification**: Internal Use Only
