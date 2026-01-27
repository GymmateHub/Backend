# GymMateHub – Comprehensive Gym Management SaaS Platform
## Business Requirements Document (BRD) v2.0

| Field | Value |
|-------|-------|
| **Version** | 2.0 |
| **Last Updated** | January 13, 2026 |
| **Status** | MVP Complete – Active Development |

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
| **Migrations** | Flyway | Schema versioning |
| **Security** | Spring Security + JWT | With TOTP 2FA support |
| **API Docs** | SpringDoc OpenAPI 3.x | Swagger UI available |
| **Payments** | Stripe (with Connect) | Platform & connected accounts |
| **Code Gen** | Lombok + MapStruct | Boilerplate reduction |
| **Containers** | Docker | Multi-stage builds |
| **CI/CD** | GitHub Actions | Automated testing & deployment |
| **Hosting** | Railway | Cloud deployment |

### 6.2 Module Architecture

The backend follows a **hexagonal/clean architecture** pattern:

```
src/main/java/com/gymmate/
├── GymMateApplication.java          # Entry point
├── shared/                          # Cross-cutting concerns
│   ├── config/                      # Configuration classes
│   ├── security/                    # JWT, Auth, Token management
│   ├── exception/                   # Global exception handling
│   ├── service/                     # Shared services (Email, Password)
│   └── multitenancy/                # Tenant context & filtering
├── organisation/                    # Organisation (tenant) management
├── user/                            # User, Member, Staff, Trainer
├── gym/                             # Gym profiles and areas
├── subscription/                    # Platform subscriptions & rate limiting
├── classes/                         # Class scheduling & bookings
├── membership/                      # Membership plans & subscriptions
├── payment/                         # Stripe, webhooks, refunds
├── inventory/                       # Equipment, stock, maintenance
└── health/                          # Exercise, workouts, metrics, goals
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
| **Payment** | PaymentController, ConnectController, StripeWebhookController, RefundControllers | `/api/payments/**`, `/api/connect/**`, `/api/webhooks/**` |
| **Inventory** | EquipmentController, InventoryController, MaintenanceController, SupplierController | `/api/equipment/**`, `/api/inventory/**`, `/api/maintenance/**` |
| **Health** | ExerciseController, WorkoutController, HealthMetricController, FitnessGoalController, HealthDashboardController | `/api/exercises/**`, `/api/workouts/**`, `/api/health/**` |

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
| Revenue tracking | ✅ | Via invoices and subscription usage |
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
| Wearable sync | ✅ | `WearableSync` entity for integration |

### 7.8 Equipment & Inventory ✅ Implemented

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Equipment tracking | ✅ | `Equipment` entity with status, warranty |
| Inventory management | ✅ | `InventoryItem`, `StockMovement` entities |
| Maintenance scheduling | ✅ | `MaintenanceRecord`, `MaintenanceSchedule` |
| Supplier management | ✅ | `Supplier` entity |

---

## 8. Non-Functional Requirements

| Requirement | Target | Implementation Status |
|-------------|--------|----------------------|
| High availability | 99.9% uptime | ✅ Railway cloud hosting |
| Horizontal scalability | Auto-scaling | ✅ Containerized deployment |
| Data isolation per tenant | Complete isolation | ✅ `organisation_id` filtering |
| GDPR-aligned data handling | Compliant | 🔄 In progress |
| Encryption (at rest) | AES-256 | ✅ Database-level |
| Encryption (in transit) | TLS 1.3 | ✅ HTTPS enforced |
| API-first architecture | RESTful | ✅ OpenAPI documented |
| Rate limiting | Per tenant | ✅ `RateLimitService` |

---

## 9. Database Schema Overview

The complete schema is defined in `src/main/resources/db/migration/V1__Complete_Schema.sql`.

### 9.1 Core Tables

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

### 9.2 Key Design Decisions

- **UUID Primary Keys**: All tables use `uuidv7()` for globally unique, time-sortable IDs
- **Soft Deletes**: `active` boolean flag on most entities
- **Audit Fields**: `created_at`, `updated_at`, `created_by` on all entities
- **Multi-tenancy**: `organisation_id` and/or `gym_id` on tenant-scoped data
- **JSON Fields**: Flexible storage for settings, preferences, certifications

---

## 10. Integrations

### 10.1 Implemented

| Integration | Provider | Purpose |
|-------------|----------|---------|
| Payment Processing | **Stripe** | Subscriptions, one-time payments, Connect for gym payouts |
| Webhook Events | **Stripe Webhooks** | Real-time payment status updates |
| Email | Configurable (Mailtrap dev) | Transactional emails, OTP |

### 10.2 Planned

| Integration | Provider | Purpose |
|-------------|----------|---------|
| SMS Notifications | Twilio | Booking reminders, alerts |
| WhatsApp Messaging | WhatsApp Business API | Member communication |
| Fitness Wearables | Apple Health, Google Fit, Fitbit | Activity data sync |
| Accounting | QuickBooks, Xero | Financial reconciliation |

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

---

## 12. Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Payment gateway downtime | Low | High | Webhook retry logic, monitoring |
| Tenant data isolation failure | Low | Critical | Strict `organisation_id` filtering, security audits |
| Regulatory changes (GDPR, etc.) | Medium | Medium | Privacy-by-design, data retention policies |
| Scalability bottlenecks | Medium | High | Load testing, horizontal scaling |
| Key personnel departure | Medium | Medium | Documentation, knowledge sharing |

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
| **Phase 3** | Q1-Q2 2026 | Enterprise & White-label | 🔄 In Progress |
| **Phase 4** | Q3-Q4 2026 | Advanced Analytics & AI | 📋 Planned |
| **Phase 5** | 2027 | Marketplace & Ecosystem | 📋 Planned |

---

## 15. Build & Run Instructions

### Prerequisites
- Java 21
- Maven 3.x (or use Maven Wrapper)
- PostgreSQL 15+ (or H2 for dev)
- `.env` file with required variables

### Commands

```bash
# Set Java 21 (if not default)
export JAVA_HOME=/path/to/java-21

# Build (skip tests)
./mvnw clean package -DskipTests

# Run tests
./mvnw test

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

## 16. Approval

This document serves as the authoritative business reference for GymMateHub development and scaling.

| Role | Name | Date |
|------|------|------|
| Product Owner | _______________ | _______________ |
| Technical Lead | _______________ | _______________ |
| Business Stakeholder | _______________ | _______________ |

---

**Document Version**: 2.0
**Last Updated**: January 13, 2026
**Classification**: Internal Use Only

