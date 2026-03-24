# GymMateHub – Product Requirements Document (PRD)
## Version 1.0 | February 5, 2026

---

## Document Information

| Field | Value |
|-------|-------|
| **Document Title** | GymMateHub Product Requirements Document |
| **Version** | 1.0 |
| **Date** | February 5, 2026 |
| **Status** | Active Development |
| **Author** | GymMateHub Development Team |
| **Classification** | Internal Use Only |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Product Vision & Goals](#2-product-vision--goals)
3. [Target Users & Personas](#3-target-users--personas)
4. [Product Architecture](#4-product-architecture)
5. [Feature Specifications](#5-feature-specifications)
6. [User Interface Requirements](#6-user-interface-requirements)
7. [API Specifications](#7-api-specifications)
8. [Data Models & Database Schema](#8-data-models--database-schema)
9. [Security Requirements](#9-security-requirements)
10. [Integration Requirements](#10-integration-requirements)
11. [Non-Functional Requirements](#11-non-functional-requirements)
12. [Testing Requirements](#12-testing-requirements)
13. [Deployment & Infrastructure](#13-deployment--infrastructure)
14. [Roadmap & Milestones](#14-roadmap--milestones)
15. [Appendices](#15-appendices)

---

## 1. Executive Summary

### 1.1 Product Overview

**GymMateHub** is a comprehensive, cloud-based, multi-tenant Gym Management Software-as-a-Service (SaaS) platform. It is designed to streamline gym operations, enhance member engagement, and provide actionable business insights for fitness businesses of all sizes.

### 1.2 Problem Statement

Fitness businesses face significant operational challenges:
- **Fragmented Tools**: Multiple disconnected systems for payments, scheduling, attendance, and communication
- **Manual Errors**: High risk of data entry mistakes and process inefficiencies
- **Poor Member Experience**: Inconsistent service delivery and limited self-service options
- **Revenue Leakage**: Missed renewals, billing errors, and lack of payment tracking
- **Limited Visibility**: No centralized view of business performance
- **Scalability Issues**: Difficulty managing multiple locations with existing tools

### 1.3 Solution

GymMateHub provides an integrated, cloud-native solution that unifies:
- Membership and subscription management
- Payment processing with Stripe integration
- Class scheduling and bookings
- Staff and trainer management
- Health and fitness tracking
- Equipment and inventory management
- Newsletter and campaign management
- Multi-channel notifications
- Business analytics and reporting

### 1.4 Current Status

| Component | Status | Completion |
|-----------|--------|------------|
| Core Platform | ✅ Complete | 100% |
| User Management | ✅ Complete | 100% |
| Membership Management | ✅ Complete | 100% |
| Payment Processing | ✅ Complete | 100% |
| Class Scheduling | ✅ Complete | 100% |
| Health & Fitness | ✅ Complete | 100% |
| Inventory Management | ✅ Complete | 100% |
| Newsletter & Campaigns | ✅ Complete | 100% |
| Multi-Channel Notifications | 🔄 Partial | 70% |
| Advanced Analytics | 📋 Planned | 0% |
| AI/ML Features | 📋 Planned | 0% |

---

## 2. Product Vision & Goals

### 2.1 Vision Statement

*"To be the definitive all-in-one platform that empowers fitness businesses to operate efficiently, engage members meaningfully, and grow sustainably through data-driven insights."*

### 2.2 Business Objectives

| Objective | Description | Success Metric |
|-----------|-------------|----------------|
| **Digitization** | Automate end-to-end gym operations | 80% reduction in manual tasks |
| **Efficiency** | Reduce administrative overhead | 50% time savings for staff |
| **Retention** | Improve member engagement and retention | >80% member retention rate |
| **Insights** | Enable data-driven decision making | 100% real-time dashboard coverage |
| **Scalability** | Support multi-location businesses | Unlimited locations per tenant |
| **Customization** | White-labeling for enterprise clients | Full branding customization |

### 2.3 Key Performance Indicators (KPIs)

| KPI | Target | Timeline |
|-----|--------|----------|
| Monthly Active Gyms (MAG) | 50+ | Q2 2026 |
| Member Retention Rate | >80% | Ongoing |
| Monthly Churn Rate | <5% | Ongoing |
| Average Revenue Per Gym (ARPG) | $200+ | Q2 2026 |
| System Uptime | 99.9% | Ongoing |
| API Response Time (P95) | <500ms | Ongoing |
| Customer Satisfaction Score | >4.5/5 | Ongoing |

---

## 3. Target Users & Personas

### 3.1 Primary User Personas

#### Persona 1: Gym Owner (Sarah)

| Attribute | Details |
|-----------|---------|
| **Role** | Single-location gym owner |
| **Age** | 35-50 |
| **Tech Proficiency** | Moderate |
| **Primary Goals** | Grow revenue, reduce overhead, understand business performance |
| **Pain Points** | Juggling multiple tools, cash flow management, member retention |
| **Key Features Needed** | Dashboard analytics, payment processing, automated billing |

#### Persona 2: Gym Manager (David)

| Attribute | Details |
|-----------|---------|
| **Role** | Day-to-day operations manager |
| **Age** | 28-40 |
| **Tech Proficiency** | High |
| **Primary Goals** | Efficient operations, staff coordination, member satisfaction |
| **Pain Points** | Manual scheduling, staff communication, reporting |
| **Key Features Needed** | Scheduling tools, member management, reporting |

#### Persona 3: Personal Trainer (Mike)

| Attribute | Details |
|-----------|---------|
| **Role** | Personal trainer and class instructor |
| **Age** | 25-35 |
| **Tech Proficiency** | High |
| **Primary Goals** | Client progress tracking, schedule management |
| **Pain Points** | Manual workout logging, client communication |
| **Key Features Needed** | Workout logging, client tracking, schedule view |

#### Persona 4: Gym Member (Lisa)

| Attribute | Details |
|-----------|---------|
| **Role** | End-user of gym services |
| **Age** | 20-55 |
| **Tech Proficiency** | Varies |
| **Primary Goals** | Easy booking, track progress, manage membership |
| **Pain Points** | Difficult booking process, no progress visibility |
| **Key Features Needed** | Class booking, health tracking, payment history |

#### Persona 5: Enterprise Administrator (Corporate Fitness)

| Attribute | Details |
|-----------|---------|
| **Role** | Multi-location chain manager |
| **Age** | 40-55 |
| **Tech Proficiency** | Moderate to High |
| **Primary Goals** | Centralized control, cross-location analytics, branding |
| **Pain Points** | Inconsistent data across locations, brand consistency |
| **Key Features Needed** | Multi-location dashboard, white-labeling, consolidated reports |

### 3.2 User Role Permissions

| Permission | OWNER | MANAGER | TRAINER | MEMBER |
|------------|-------|---------|---------|--------|
| View Dashboard | ✅ | ✅ | ❌ | ❌ |
| Manage Members | ✅ | ✅ | ❌ | ❌ |
| Manage Staff | ✅ | ✅ | ❌ | ❌ |
| Manage Classes | ✅ | ✅ | ✅ | ❌ |
| View Analytics | ✅ | ✅ | ❌ | ❌ |
| Process Payments | ✅ | ✅ | ❌ | ❌ |
| Manage Inventory | ✅ | ✅ | ❌ | ❌ |
| Book Classes | ✅ | ✅ | ✅ | ✅ |
| Log Workouts | ✅ | ✅ | ✅ | ✅ |
| View Own Profile | ✅ | ✅ | ✅ | ✅ |
| Send Newsletters | ✅ | ✅ | ❌ | ❌ |
| Manage Subscriptions | ✅ | ❌ | ❌ | ❌ |

---

## 4. Product Architecture

### 4.1 Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Runtime** | Java | 21 (LTS) | Application runtime |
| **Framework** | Spring Boot | 3.5.6 | Application framework |
| **Build Tool** | Maven | 3.x | Dependency management |
| **Database** | PostgreSQL | 15+ | Production database |
| **Dev Database** | H2 | Latest | In-memory testing |
| **Migrations** | Flyway | Latest | Schema versioning |
| **Security** | Spring Security | 6.x | Authentication/Authorization |
| **JWT** | jjwt | 0.12.x | Token management |
| **API Docs** | SpringDoc OpenAPI | 3.x | API documentation |
| **Payments** | Stripe | Latest | Payment processing |
| **Caching** | Redis | Latest | Session & rate limiting |
| **Code Gen** | Lombok + MapStruct | Latest | Boilerplate reduction |
| **Containers** | Docker | Latest | Containerization |
| **CI/CD** | GitHub Actions | N/A | Automation |
| **Hosting** | Railway | N/A | Cloud deployment |

### 4.2 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT APPLICATIONS                           │
│   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │
│   │   Web App     │  │  Mobile App   │  │ Third-Party   │          │
│   │   (React)     │  │  (iOS/Android)│  │    APIs       │          │
│   └───────┬───────┘  └───────┬───────┘  └───────┬───────┘          │
└───────────┼──────────────────┼──────────────────┼───────────────────┘
            │                  │                  │
            └──────────────────┼──────────────────┘
                               │ HTTPS/REST
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                                  │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                    Spring Security                           │  │
│   │  • JWT Authentication  • Role-Based Access  • Rate Limiting │  │
│   └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                          │
│                                                                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │Organisation │ │    User     │ │  Membership │ │   Payment   │  │
│  │   Module    │ │   Module    │ │   Module    │ │   Module    │  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │   Classes   │ │   Health    │ │  Inventory  │ │Notification │  │
│  │   Module    │ │   Module    │ │   Module    │ │   Module    │  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │Subscription │ │     Gym     │ │   Shared    │ │   Future    │  │
│  │   Module    │ │   Module    │ │   Module    │ │  (AI, etc.) │  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   PostgreSQL    │  │      Redis      │  │    External     │
│   Database      │  │     Cache       │  │   Services      │
│                 │  │                 │  │  • Stripe       │
│  • 40+ Tables   │  │  • Sessions     │  │  • SMTP         │
│  • Flyway       │  │  • Rate Limits  │  │  • Twilio       │
│  • Multi-tenant │  │  • Token Cache  │  │  • WhatsApp     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 4.3 Module Architecture

Each module follows a **hexagonal/clean architecture** pattern:

```
module/
├── api/                    # Presentation Layer
│   ├── controllers/        # REST endpoints
│   └── dto/               # Request/Response objects
├── application/           # Application Layer
│   ├── services/          # Business logic
│   └── mappers/           # Entity-DTO mapping
├── domain/                # Domain Layer
│   ├── entities/          # JPA entities
│   ├── enums/             # Domain enums
│   └── events/            # Domain events
└── infrastructure/        # Infrastructure Layer
    ├── repositories/      # Data access
    └── adapters/          # External integrations
```

### 4.4 Module Summary

| Module | Files | Description |
|--------|-------|-------------|
| **shared** | 57 | Cross-cutting concerns (config, security, exceptions) |
| **organisation** | 11 | Tenant management |
| **user** | 33 | User, Member, Staff, Trainer management |
| **gym** | 15 | Gym profiles and areas |
| **subscription** | 22 | Platform subscriptions and rate limiting |
| **classes** | 46 | Class scheduling and bookings |
| **membership** | 30 | Membership plans and member subscriptions |
| **payment** | 38 | Stripe integration, webhooks, refunds |
| **inventory** | 50 | Equipment, stock, maintenance |
| **health** | 67 | Exercise, workouts, metrics, goals |
| **notification** | 35 | Newsletter and multi-channel messaging |

---

## 5. Feature Specifications

### 5.1 Multi-Tenant Management

#### 5.1.1 Organisation Management

**Description**: Core tenant isolation and management system.

| Feature | Status | Description |
|---------|--------|-------------|
| Organisation Creation | ✅ | Register new gym organisations |
| Tenant Isolation | ✅ | Data separation via `organisation_id` |
| Subscription Tiers | ✅ | Starter, Professional, Enterprise, Custom |
| Rate Limiting | ✅ | Per-tenant API rate limits |
| Custom Branding | ✅ | Gym-level logo and settings |

**Entities**:
- `Organisation` - Root tenant entity
- `Gym` - Individual gym location
- `GymArea` - Physical areas within a gym

#### 5.1.2 Subscription Tiers

| Tier | Price/Month | Members | API/Hour | SMS/Month | Email/Month |
|------|-------------|---------|----------|-----------|-------------|
| **Starter** | $99 | 200 | 1,000 | 500 | 1,000 |
| **Professional** | $199 | 500 | 5,000 | 2,000 | 5,000 |
| **Enterprise** | $399 | Unlimited | 25,000 | 10,000 | 25,000 |
| **Custom** | $999+ | Unlimited | Unlimited | 50,000 | 100,000 |

### 5.2 User Management & Authentication

#### 5.2.1 Authentication System

| Feature | Status | Implementation |
|---------|--------|----------------|
| JWT Authentication | ✅ | Access + Refresh tokens |
| Role-Based Access | ✅ | OWNER, STAFF, TRAINER, MEMBER |
| Password Reset | ✅ | Email-based with tokens |
| Two-Factor Auth | ✅ | TOTP (Time-based OTP) |
| Token Blacklisting | ✅ | Logout and invalidation |
| Session Management | ✅ | Redis-backed sessions |

#### 5.2.2 User Types

| User Type | Entity | Key Fields |
|-----------|--------|------------|
| **User** | `User` | email, password, role, 2FA settings |
| **Member** | `Member` | health info, emergency contacts |
| **Staff** | `Staff` | position, department, hire date |
| **Trainer** | `Trainer` | specializations, certifications |

### 5.3 Membership Management

#### 5.3.1 Membership Plans

| Feature | Status | Description |
|---------|--------|-------------|
| Plan Creation | ✅ | Create pricing plans |
| Duration Options | ✅ | Daily, weekly, monthly, annual |
| Pricing Tiers | ✅ | Multiple price points |
| Feature Flags | ✅ | Plan-specific features |

#### 5.3.2 Member Memberships

| Feature | Status | Description |
|---------|--------|-------------|
| Enrollment | ✅ | Member joins a plan |
| Status Tracking | ✅ | ACTIVE, FROZEN, EXPIRED, CANCELLED |
| Freezing | ✅ | Pause membership temporarily |
| Renewals | ✅ | Manual or automatic |
| Invoicing | ✅ | Generate member invoices |

**Status Lifecycle**:
```
PENDING → ACTIVE → FROZEN → ACTIVE → EXPIRED
                        ↓
                   CANCELLED
```

### 5.4 Payment Processing

#### 5.4.1 Stripe Integration

| Feature | Status | Description |
|---------|--------|-------------|
| Platform Payments | ✅ | Gym pays GymMate subscription |
| Stripe Connect | ✅ | Gyms receive member payments |
| Payment Intents | ✅ | One-time payments |
| Subscriptions | ✅ | Recurring billing |
| Webhooks | ✅ | Real-time event handling |
| Refunds | ✅ | Full and partial refunds |

#### 5.4.2 Payment Flow

```
Member → Stripe Elements → PaymentMethod → Backend → Stripe API
                                              ↓
                                        Gym Connect Account
                                              ↓
                                      Platform Fee (1%)
```

### 5.5 Class Scheduling & Bookings

#### 5.5.1 Class Management

| Feature | Status | Description |
|---------|--------|-------------|
| Class Categories | ✅ | Yoga, HIIT, Spinning, etc. |
| Class Creation | ✅ | Define class templates |
| Schedule Creation | ✅ | Recurring or one-time schedules |
| Trainer Assignment | ✅ | Link trainers to schedules |
| Capacity Management | ✅ | Set and enforce limits |
| Gym Area Assignment | ✅ | Assign rooms/areas |

#### 5.5.2 Booking System

| Feature | Status | Description |
|---------|--------|-------------|
| Book Class | ✅ | Member books a spot |
| Cancel Booking | ✅ | Member cancels |
| Waitlist | ✅ | Queue when class is full |
| Check-in | ✅ | Mark attendance |
| No-show Tracking | ✅ | Track missed bookings |

**Booking Status Flow**:
```
PENDING → CONFIRMED → CHECKED_IN
    ↓          ↓
CANCELLED  NO_SHOW
```

### 5.6 Health & Fitness Tracking

#### 5.6.1 Exercise Library

| Feature | Status | Description |
|---------|--------|-------------|
| Exercise Categories | ✅ | Strength, Cardio, Flexibility |
| Exercise Database | ✅ | Pre-built and custom exercises |
| Instructions | ✅ | Text and media instructions |
| Muscle Groups | ✅ | Target muscle mapping |

#### 5.6.2 Workout Logging

| Feature | Status | Description |
|---------|--------|-------------|
| Workout Logs | ✅ | Record workout sessions |
| Exercise Entries | ✅ | Sets, reps, weight, duration |
| Notes | ✅ | Session notes |
| Duration Tracking | ✅ | Total workout time |

#### 5.6.3 Health Metrics

| Metric Type | Status | Description |
|-------------|--------|-------------|
| Weight | ✅ | Body weight tracking |
| Body Fat % | ✅ | Body composition |
| BMI | ✅ | Body Mass Index |
| Heart Rate | ✅ | Resting and active |
| Blood Pressure | ✅ | Systolic/Diastolic |
| Sleep | ✅ | Hours and quality |

#### 5.6.4 Fitness Goals

| Feature | Status | Description |
|---------|--------|-------------|
| Goal Types | ✅ | Weight loss, muscle gain, etc. |
| Target Setting | ✅ | Numeric targets |
| Progress Tracking | ✅ | Current vs target |
| Completion | ✅ | Mark goals complete |

### 5.7 Equipment & Inventory

#### 5.7.1 Equipment Management

| Feature | Status | Description |
|---------|--------|-------------|
| Equipment Registry | ✅ | Track all gym equipment |
| Status Tracking | ✅ | ACTIVE, MAINTENANCE, RETIRED |
| Warranty Tracking | ✅ | Expiration dates |
| Location Assignment | ✅ | Assign to gym areas |

#### 5.7.2 Inventory Management

| Feature | Status | Description |
|---------|--------|-------------|
| Inventory Items | ✅ | Consumables and supplies |
| Stock Levels | ✅ | Current quantity tracking |
| Reorder Points | ✅ | Low stock alerts |
| Stock Movements | ✅ | In/out tracking |
| Supplier Management | ✅ | Vendor database |

#### 5.7.3 Maintenance

| Feature | Status | Description |
|---------|--------|-------------|
| Maintenance Records | ✅ | Log repairs and service |
| Scheduled Maintenance | ✅ | Recurring maintenance |
| Cost Tracking | ✅ | Maintenance expenses |

### 5.8 Newsletter & Campaign Management

#### 5.8.1 Newsletter Templates

| Feature | Status | Description |
|---------|--------|-------------|
| Template Creation | ✅ | HTML email templates |
| Placeholders | ✅ | Dynamic content variables |
| Categories | ✅ | Organize templates |
| Preview | ✅ | View before sending |

#### 5.8.2 Campaign Management

| Feature | Status | Description |
|---------|--------|-------------|
| Campaign Creation | ✅ | Create email campaigns |
| Audience Targeting | ✅ | Filter recipients |
| Scheduling | ✅ | Send now or later |
| Tracking | ✅ | Delivery status per recipient |

**Audience Types**:
- `ALL_MEMBERS` - All gym members
- `ACTIVE_MEMBERS` - Members with active memberships
- `INACTIVE_MEMBERS` - Expired or frozen memberships
- `NEW_MEMBERS` - Recently joined
- `CLASS_ATTENDEES` - Specific class enrollees (pending)
- `MEMBERSHIP_PLAN` - Specific plan members (pending)

### 5.9 Multi-Channel Notifications

#### 5.9.1 Channel Support

| Channel | Status | Implementation |
|---------|--------|----------------|
| Email | ✅ Complete | SMTP (Mailtrap dev) |
| SMS | 🔄 Infrastructure | Twilio stub ready |
| WhatsApp | 🔄 Infrastructure | API stub ready |
| Push | 📋 Planned | Not started |

#### 5.9.2 Notification Architecture

```
BroadcastService (Orchestrator)
        │
        ├── AudienceResolver (Filter members)
        │
        └── ChannelSender (Interface)
                ├── EmailChannelSender ✅
                ├── SmsChannelSender ⚠️ Stub
                └── WhatsAppChannelSender ⚠️ Stub
```

**Fallback Behavior**: If preferred channel fails, system falls back to email.

---

## 6. User Interface Requirements

### 6.1 Web Application (Admin Portal)

#### 6.1.1 Dashboard

| Component | Description |
|-----------|-------------|
| Overview Cards | Active members, revenue, bookings, classes today |
| Revenue Chart | Monthly revenue trends |
| Member Chart | Member growth over time |
| Recent Activity | Latest bookings, payments, sign-ups |
| Quick Actions | Add member, create class, process payment |

#### 6.1.2 Navigation Structure

```
Dashboard
├── Members
│   ├── All Members
│   ├── Add Member
│   └── Member Details
├── Classes
│   ├── Schedule
│   ├── Categories
│   └── Bookings
├── Staff
│   ├── All Staff
│   └── Trainers
├── Payments
│   ├── Transactions
│   ├── Invoices
│   └── Refunds
├── Inventory
│   ├── Equipment
│   ├── Supplies
│   └── Maintenance
├── Marketing
│   ├── Campaigns
│   └── Templates
├── Reports
│   └── Analytics
└── Settings
    ├── Gym Profile
    ├── Subscription
    └── Integrations
```

### 6.2 Member Portal

| Feature | Description |
|---------|-------------|
| Profile | View/edit personal information |
| Classes | Browse and book classes |
| Schedule | View upcoming bookings |
| Workouts | Log and view workout history |
| Goals | Set and track fitness goals |
| Health | View health metrics |
| Payments | View payment history |

### 6.3 Mobile Application (Future)

- Native iOS and Android apps
- Core features: booking, check-in, workout logging
- Push notifications
- Wearable device sync

---

## 7. API Specifications

### 7.1 API Overview

| Aspect | Details |
|--------|---------|
| **Protocol** | RESTful HTTP/HTTPS |
| **Format** | JSON |
| **Authentication** | JWT Bearer Token |
| **Documentation** | OpenAPI 3.x (Swagger UI) |
| **Versioning** | URL path (e.g., `/api/v1/`) |
| **Rate Limiting** | Per-tenant based on subscription |

### 7.2 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Authenticate user |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Invalidate tokens |
| POST | `/api/auth/password/reset-request` | Request password reset |
| POST | `/api/auth/password/reset` | Reset password |
| POST | `/api/auth/2fa/setup` | Setup 2FA |
| POST | `/api/auth/2fa/verify` | Verify 2FA code |

### 7.3 Core API Endpoints

#### Organisation & Gym
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/organisations/{id}` | Get organisation |
| PUT | `/api/organisations/{id}` | Update organisation |
| GET | `/api/gyms` | List gyms |
| POST | `/api/gyms` | Create gym |
| GET | `/api/gyms/{id}` | Get gym details |

#### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List users |
| GET | `/api/users/{id}` | Get user |
| PUT | `/api/users/{id}` | Update user |
| GET | `/api/members` | List members |
| POST | `/api/members` | Create member |
| GET | `/api/staff` | List staff |
| GET | `/api/trainers` | List trainers |

#### Classes
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/classes` | List classes |
| POST | `/api/classes` | Create class |
| GET | `/api/schedules` | List schedules |
| POST | `/api/schedules` | Create schedule |
| GET | `/api/bookings` | List bookings |
| POST | `/api/bookings` | Create booking |
| DELETE | `/api/bookings/{id}` | Cancel booking |

#### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create-intent` | Create payment intent |
| GET | `/api/payments/history` | Payment history |
| POST | `/api/connect/onboard` | Start Stripe Connect |
| GET | `/api/connect/status` | Connect account status |
| POST | `/api/webhooks/stripe/platform` | Platform webhook |
| POST | `/api/webhooks/stripe/connect` | Connect webhook |

#### Health & Fitness
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/exercises` | List exercises |
| POST | `/api/workouts` | Log workout |
| GET | `/api/workouts` | Get workout history |
| POST | `/api/health/metrics` | Log health metric |
| GET | `/api/health/metrics` | Get metrics history |
| POST | `/api/health/goals` | Create fitness goal |

#### Newsletter
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/newsletters/templates` | List templates |
| POST | `/api/newsletters/templates` | Create template |
| GET | `/api/campaigns` | List campaigns |
| POST | `/api/campaigns` | Create campaign |
| POST | `/api/campaigns/{id}/send` | Send campaign |

### 7.4 Response Format

**Success Response**:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-02-05T14:23:08Z"
}
```

**Error Response**:
```json
{
  "success": false,
  "error": "Error type",
  "message": "Detailed error message",
  "timestamp": "2026-02-05T14:23:08Z",
  "path": "/api/endpoint"
}
```

### 7.5 Rate Limit Headers

```http
X-RateLimit-Limit-Hourly: 5000
X-RateLimit-Remaining-Hourly: 4500
X-RateLimit-Limit-Burst: 500
X-RateLimit-Remaining-Burst: 450
```

---

## 8. Data Models & Database Schema

### 8.1 Schema Overview

| Domain | Tables | Key Entities |
|--------|--------|--------------|
| **Organisation** | 3 | organisations, gyms, gym_areas |
| **Users** | 4 | users, staff, trainers, members |
| **Subscriptions** | 4 | subscription_tiers, subscriptions, usage, rate_limits |
| **Classes** | 4 | categories, classes, schedules, bookings |
| **Membership** | 5 | plans, memberships, freeze_policies, invoices, payment_methods |
| **Payments** | 6 | payment_methods, invoices, refunds, requests, audit_log, webhook_events |
| **Health** | 8 | categories, exercises, workout_logs, workout_exercises, metrics, goals, photos, wearable_syncs |
| **Inventory** | 6 | equipment, items, movements, maintenance_records, schedules, suppliers |
| **Security** | 3 | pending_registrations, password_reset_tokens, token_blacklist |
| **Newsletter** | 3 | templates, campaigns, recipients |

**Total Tables: 46**

### 8.2 Key Design Decisions

| Decision | Implementation | Rationale |
|----------|----------------|-----------|
| **UUID v7 Primary Keys** | `uuidv7()` function | Time-sortable, globally unique |
| **Soft Deletes** | `is_active` boolean | Data recovery, audit trail |
| **Audit Fields** | `created_at`, `updated_at`, `created_by`, `updated_by` | Compliance, debugging |
| **Multi-tenancy** | `organisation_id` on all tenant data | Data isolation |
| **JSON Fields** | `jsonb` for settings, preferences | Flexibility |

### 8.3 Entity Relationships

```
Organisation (1) ──< (N) Gyms ──< (N) GymAreas
      │
      └──< (N) Users
              ├── Member (1:1)
              ├── Staff (1:1)
              └── Trainer (1:1)

Gym (1) ──< (N) Classes ──< (N) ClassSchedules ──< (N) ClassBookings
                                    │
                                    └── Trainer (N:1)

Member (1) ──< (N) MemberMemberships ──< (1) MembershipPlan
       │
       ├──< (N) WorkoutLogs ──< (N) WorkoutExercises
       ├──< (N) HealthMetrics
       ├──< (N) FitnessGoals
       └──< (N) ClassBookings

Subscription (1) ──< (1) SubscriptionTier
             │
             └──< (N) SubscriptionUsage

NewsletterCampaign (1) ──< (N) CampaignRecipients
                   │
                   └── NewsletterTemplate (N:1)
```

### 8.4 Flyway Migrations

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__Complete_Schema.sql` | Core schema (46KB) |
| V1.1 | `V1_1__Add_Missing_Columns_To_Equipment.sql` | Equipment fixes |
| V2 | `V2__Newsletter_Tables.sql` | Newsletter feature |
| V3 | `V3__Multi_Channel_Support.sql` | Multi-channel support |

---

## 9. Security Requirements

### 9.1 Authentication & Authorization

| Requirement | Implementation | Status |
|-------------|----------------|--------|
| JWT Tokens | Access (15min) + Refresh (7d) | ✅ |
| Password Hashing | BCrypt | ✅ |
| Role-Based Access | Spring Security annotations | ✅ |
| Two-Factor Auth | TOTP (RFC 6238) | ✅ |
| Token Blacklisting | Database-backed | ✅ |
| Session Management | Redis | ✅ |

### 9.2 Data Protection

| Requirement | Implementation | Status |
|-------------|----------------|--------|
| Encryption at Rest | Database-level AES-256 | ✅ |
| Encryption in Transit | TLS 1.3 | ✅ |
| Multi-tenant Isolation | `organisation_id` filtering | ✅ |
| Input Validation | Bean Validation | ✅ |
| SQL Injection Prevention | Parameterized queries | ✅ |
| XSS Prevention | Spring Security defaults | ✅ |

### 9.3 Compliance (Pending)

| Requirement | Status | Notes |
|-------------|--------|-------|
| GDPR Data Export | ❌ | Right to access |
| GDPR Data Deletion | ❌ | Right to erasure |
| Consent Management | ❌ | Required for EU |
| Data Retention Policies | ❌ | Automated cleanup |
| Audit Logging (PII) | ❌ | Access tracking |

---

## 10. Integration Requirements

### 10.1 Implemented Integrations

#### 10.1.1 Stripe (Payment Processing)

| Feature | Endpoint | Status |
|---------|----------|--------|
| Create Customer | Stripe API | ✅ |
| Payment Intents | Stripe API | ✅ |
| Subscriptions | Stripe API | ✅ |
| Connect Accounts | Stripe Connect | ✅ |
| Platform Webhooks | `/api/webhooks/stripe/platform` | ✅ |
| Connect Webhooks | `/api/webhooks/stripe/connect` | ✅ |

#### 10.1.2 Email (SMTP)

| Feature | Provider | Status |
|---------|----------|--------|
| Transactional Email | SMTP (Mailtrap) | ✅ |
| Newsletter Campaigns | SMTP | ✅ |
| OTP Delivery | SMTP | ✅ |

### 10.2 Planned Integrations

| Integration | Provider | Purpose | Status |
|-------------|----------|---------|--------|
| SMS | Twilio | Notifications, reminders | 🔄 Stub ready |
| WhatsApp | WhatsApp Business API | Member communication | 🔄 Stub ready |
| Wearables | Apple Health, Google Fit, Fitbit | Activity sync | 📋 Schema only |
| Accounting | QuickBooks, Xero | Financial reconciliation | 📋 Not started |
| Access Control | Various | Biometric/card access | 📋 Not started |

---

## 11. Non-Functional Requirements

### 11.1 Performance

| Metric | Target | Current |
|--------|--------|---------|
| API Response Time (P95) | <500ms | ✅ Achieved |
| Database Query Time | <100ms | ✅ Achieved |
| Page Load Time | <3s | N/A (Backend only) |
| Concurrent Users | 1000+ | ✅ Scalable |

### 11.2 Availability

| Metric | Target | Implementation |
|--------|--------|----------------|
| Uptime | 99.9% | Railway cloud hosting |
| Disaster Recovery | RTO <4h | Database backups |
| Failover | Automatic | Container orchestration |

### 11.3 Scalability

| Aspect | Implementation |
|--------|----------------|
| Horizontal Scaling | Docker containers |
| Database Scaling | PostgreSQL replication |
| Caching | Redis cluster |
| Load Balancing | Railway / Cloud provider |

### 11.4 Maintainability

| Aspect | Implementation |
|--------|----------------|
| Code Quality | Clean architecture, SOLID principles |
| Documentation | OpenAPI, inline comments |
| Logging | Structured logging (SLF4J) |
| Monitoring | Actuator endpoints |

---

## 12. Testing Requirements

### 12.1 Current Test Coverage

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

**Overall: 24 test files, 12% service coverage**

### 12.2 Testing Strategy

| Type | Tool | Coverage Target |
|------|------|-----------------|
| Unit Tests | JUnit 5, Mockito | 80% |
| Integration Tests | Spring Boot Test | 60% |
| API Tests | MockMvc, RestAssured | 100% endpoints |
| E2E Tests | Selenium/Cypress | Critical flows |

### 12.3 Critical Test Gaps

- ❌ Authentication & Security services
- ❌ Payment processing services
- ❌ User management services
- ❌ Subscription services

---

## 13. Deployment & Infrastructure

### 13.1 Environments

| Environment | Purpose | Database | URL |
|-------------|---------|----------|-----|
| Local | Development | H2 / PostgreSQL | localhost:8080 |
| Dev | Integration testing | PostgreSQL | dev.gymmatehub.com |
| Staging | Pre-production | PostgreSQL | staging.gymmatehub.com |
| Production | Live | PostgreSQL | api.gymmatehub.com |

### 13.2 CI/CD Pipeline

```yaml
Workflow: main.yml
Triggers: push/PR to main, dev branches

Jobs:
1. build
   - Checkout code
   - Setup JDK 21
   - Build with Maven (skip tests)
   - Run tests with H2

2. docker (on push to main/dev)
   - Build multi-stage Docker image
   - Push to Docker Hub

3. deploy-to-railway (on push to main/dev)
   - Deploy to Railway cloud
```

### 13.3 Docker Configuration

```dockerfile
# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 13.4 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DATABASE_URL` | PostgreSQL connection string | Yes |
| `REDIS_URL` | Redis connection string | Yes |
| `STRIPE_API_KEY` | Stripe secret key | Yes |
| `STRIPE_WEBHOOK_SECRET` | Platform webhook secret | Yes |
| `STRIPE_CONNECT_WEBHOOK_SECRET` | Connect webhook secret | Yes |
| `JWT_SECRET` | JWT signing key | Yes |
| `MAIL_HOST` | SMTP server host | Yes |
| `MAIL_USERNAME` | SMTP username | Yes |
| `MAIL_PASSWORD` | SMTP password | Yes |
| `FRONTEND_URL` | Frontend application URL | Yes |

---

## 14. Roadmap & Milestones

### 14.1 Completed Phases

| Phase | Timeline | Focus | Status |
|-------|----------|-------|--------|
| **Phase 1** | Q1-Q2 2025 | MVP Development | ✅ Complete |
| **Phase 2** | Q3-Q4 2025 | Feature Expansion | ✅ Complete |
| **Phase 2.5** | Q1 2026 | Newsletter & Notifications | ✅ Complete |

### 14.2 Current Phase

**Phase 3: Enterprise & White-label** (Q1-Q2 2026)

| Feature | Status | Target Date |
|---------|--------|-------------|
| SMS Integration (Twilio) | 🔄 In Progress | Feb 2026 |
| WhatsApp Integration | 🔄 In Progress | Mar 2026 |
| GDPR Compliance | 📋 Planned | Mar 2026 |
| White-label Branding | 📋 Planned | Apr 2026 |
| Multi-location Dashboard | 📋 Planned | May 2026 |

### 14.3 Future Phases

**Phase 4: Advanced Analytics & AI** (Q3-Q4 2026)
- Predictive churn analysis
- AI workout recommendations
- Member engagement scoring
- Revenue forecasting

**Phase 5: Marketplace & Ecosystem** (2027)
- Third-party app integrations
- Plugin marketplace
- Partner API access
- Franchise management tools

---

## 15. Appendices

### 15.1 Glossary

| Term | Definition |
|------|------------|
| **Organisation** | Top-level tenant entity (the fitness business) |
| **Gym** | Individual gym location within an organisation |
| **Member** | End-user who uses gym services |
| **Trainer** | Staff member who conducts classes and personal training |
| **Subscription** | Platform subscription (Gym pays GymMateHub) |
| **Membership** | Member subscription (Member pays Gym) |
| **Connect** | Stripe Connect for gym payment processing |
| **Campaign** | Newsletter email campaign |
| **TOTP** | Time-based One-Time Password (2FA) |

### 15.2 References

| Document | Location |
|----------|----------|
| BRD v2.1 | `docs/GymMateHub_brd_v2.md` |
| BRD v2.0 (Archived) | `docs/GymMateHub_brd_v2.0_archived.md` |
| Stripe Integration Guide | `docs/stripe_integration_guide.md` |
| Subscription Guide | `docs/subscription_and_rate_limiting_guide.md` |
| OpenAPI Docs | `/swagger-ui.html` |

### 15.3 Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | _______________ | _______________ | _______________ |
| Technical Lead | _______________ | _______________ | _______________ |
| Business Stakeholder | _______________ | _______________ | _______________ |

---

**Document Version**: 1.0
**Created**: February 5, 2026
**Classification**: Internal Use Only

© 2026 GymMateHub. All Rights Reserved.
