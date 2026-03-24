# GymMate Backend

Multi-tenant SaaS gym management platform built as a Spring Boot modular monolith.

## Tech Stack

- **Java 21** / **Spring Boot 3.5.6**
- **PostgreSQL** (H2 for local dev) with **Flyway** migrations
- **Spring Security** + JWT authentication
- **Stripe** for payment processing
- **SpringDoc OpenAPI** (Swagger)
- **Lombok** + **MapStruct**
- **Maven Wrapper** (`./mvnw`)

## Quick Start

### 1. Prerequisites

- JDK 21
- PostgreSQL 15+ (optional — H2 works for local dev)

### 2. Environment Setup

```bash
cp .env.example .env
```

Edit `.env` with your values. The minimum required variables are already set for H2 local development.

### 3. Build & Run

```bash
# Verify Java version
java -version   # Must be 21

# Build (skip tests)
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

### 4. Verify

- **Health:** http://localhost:8080/actuator/health
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/v3/api-docs

## Project Structure

```
src/main/java/com/gymmate/
├── shared/              # Cross-cutting: config, security, exceptions, DTOs
├── user/                # User management (owners, staff, trainers, members)
├── organisation/        # Organisation (multi-tenant parent entity)
├── gym/                 # Gym management
├── membership/          # Membership plans & subscriptions
├── classes/             # Class scheduling & bookings
├── booking/             # Session booking
├── payment/             # Stripe payments, invoices, refunds
├── inventory/           # Equipment & resource tracking
├── analytics/           # Business insights & reporting
├── notification/        # Email, SSE, push notifications
├── newsletter/          # Newsletter & campaign management
└── subscription/        # Platform subscription tiers & rate limiting
```

Each module follows clean/hexagonal architecture:

```
module/
├── api/              # Controllers & request/response DTOs
├── application/      # Service layer & use cases
├── domain/           # Entities, value objects, repository interfaces
└── infrastructure/   # JPA repositories, adapters, external integrations
```

## Key Endpoints

| Area | Endpoint | Description |
|------|----------|-------------|
| Auth | `POST /api/auth/login` | Login |
| Auth | `POST /api/auth/refresh-token` | Refresh JWT |
| Users | `POST /api/users/register/gym-owner` | Register gym owner |
| Gyms | `POST /api/gyms/register` | Register a gym |
| Gyms | `GET /api/gyms` | List gyms |
| Members | `GET /api/members` | List members |
| Classes | `GET /api/classes` | List classes |
| Payments | `POST /api/payments` | Process payment |

Full API reference available at `/swagger-ui.html` when the app is running.

## Database Migrations

Migration files live in `src/main/resources/db/migration/`. Flyway is disabled by default in dev (`FLYWAY_ENABLED=false`, `JPA_DDL_AUTO=update`). For production, enable Flyway and set `JPA_DDL_AUTO=validate`.

## Testing

```bash
./mvnw test
```

## Docker

```bash
docker-compose up -d
```

Or build the image directly:

```bash
docker build -t gymmate-backend .
```

## Documentation

Detailed documentation lives in the [`docs/`](docs/) folder:

| Document | Description |
|----------|-------------|
| [PRODUCT_STATE_REPORT.md](docs/PRODUCT_STATE_REPORT.md) | Full codebase audit — module status, tech debt, production readiness |
| [GymMateHub_PRD_v1.0.md](docs/GymMateHub_PRD_v1.0.md) | Product requirements — feature specs, data models, API specs |
| [GymMateHub_brd_v2.md](docs/GymMateHub_brd_v2.md) | Business requirements — strategy, module architecture, roadmap |

## Contributing

1. Create a feature branch (`git checkout -b feature/your-feature`)
2. Commit your changes (`git commit -m 'Add your feature'`)
3. Push to the branch (`git push origin feature/your-feature`)
4. Open a Pull Request

## License

MIT
