# GymMate Backend - Copilot Coding Instructions

## Repository Overview
**GymMate Backend** is a Spring Boot modular monolith for gym management with **98 Java files** using clean/hexagonal architecture.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Maven Wrapper, PostgreSQL/H2, Flyway, JWT + Spring Security, SpringDoc OpenAPI, Lombok + MapStruct

## Critical Build Requirements

### Java Version
**ALWAYS use Java 21.** The default system Java may be 17, which will cause compilation failures.

```bash
# Linux/Ubuntu
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# macOS (adjust path to your installation)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Build Commands
1. **Build (no tests):** `./mvnw clean package -DskipTests` (~18-30s) → `target/gymmate-backend-0.0.1-SNAPSHOT.jar`
2. **Build + install (CI):** `./mvnw clean install` (~20-35s)
3. **Run tests:** `./mvnw test` (no tests exist yet)
4. **Run app:** `./mvnw spring-boot:run` (port 8080, requires `.env`)

### Environment Setup
**REQUIRED:** `cp .env.example .env`

**Minimum required variables:**
```properties
# JWT Configuration
JWT_SECRET=gymmate_jwt_secret_key_change_me
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Database (H2 for dev)
PG_URI=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DB_DRIVER=org.h2.Driver
PG_USER=sa
PG_PASSWORD=
DB_DIALECT=org.hibernate.dialect.H2Dialect

# Application
APP_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
CORS_ALLOW_CREDENTIALS=true

# Logging
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_GYMMATE=DEBUG
```

**Additional variables needed for full functionality:**
```properties
# Redis (optional in dev)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Email (Mailtrap for dev)
MAILTRAP_HOST=sandbox.smtp.mailtrap.io
MAILTRAP_PORT=2525
MAILTRAP_USERNAME=
MAILTRAP_PASSWORD=

# File Upload
FILE_UPLOAD_DIR=./uploads
FILE_MAX_SIZE=10485760
FILE_MAX_REQUEST_SIZE=10485760

# Database settings
SHOW_SQL=false
FLYWAY_ENABLED=false
JPA_DDL_AUTO=update
```

**Note:** `.env` loaded via `dotenv-java` in `GymMateApplication.main()`. Missing `.env` causes startup failure.

## Project Architecture

### Module Structure
```
src/main/java/com/gymmate/
├── GymMateApplication.java       # Entry point (@SpringBootApplication, @EnableScheduling)
│
├── shared/                       # Cross-cutting concerns
│   ├── config/                   # SecurityConfig, CorsConfig, JpaAuditingConfig, etc.
│   ├── security/                 # JWT, auth controllers, filters, token management
│   ├── exception/                # GlobalExceptionHandler, custom exceptions
│   ├── service/                  # EmailService, PasswordService
│   ├── multitenancy/             # TenantContext, TenantFilter
│   ├── dto/                      # ApiResponse, ErrorResponse
│   └── domain/                   # BaseEntity, BaseAuditEntity, TenantEntity
│
├── user/                         # User Management Module
│   ├── api/                      # Controllers: UserController, MemberController, StaffController, TrainerController
│   ├── application/              # Services: UserService, MemberService, StaffService, TrainerService
│   ├── domain/                   # Entities: User, Member, Staff, Trainer + enums
│   └── infrastructure/           # JPA repositories
│
├── Gym/                          # Gym Management Module (capital G)
│   ├── api/                      # GymController + DTOs
│   ├── application/              # GymService
│   ├── domain/                   # Gym, Address, GymStatus, GymRepository interface
│   └── infrastructure/           # GymJpaRepository, GymRepositoryAdapter
│
├── membership/                   # Membership domain entities
├── classes/                      # Class scheduling domain entities
├── booking/                      # (placeholder)
├── payment/                      # (placeholder)
├── inventory/                    # (placeholder)
├── analytics/                    # (placeholder)
└── notification/                 # (placeholder)
```

**Note:** `booking`, `payment`, `inventory`, `analytics`, `notification` modules are documented but not yet implemented.

### Key Files
- `pom.xml`, `application.yml`, `application-dev.yml`, `src/main/resources/db/migration/`, `.editorconfig` (2 spaces, LF)
- `GymMateApplication.java` - Loads `.env`, starts app
- `shared/config/SecurityConfig.java` - Security + JWT config
- `shared/exception/GlobalExceptionHandler.java` - Exception handling
- `user/domain/User.java` - User entity (roles: SUPER_ADMIN, GYM_OWNER, STAFF, TRAINER, MEMBER)

## CI/CD Pipeline

### GitHub Actions (`.github/workflows/main.yml`)
**Triggers:** Push/PR to `main` or `dev`

**Build job:** Checkout → Setup JDK 21 (Temurin) + Maven cache → `chmod +x mvnw` → `./mvnw clean install` → `./mvnw -B test` → Archive JAR + test reports

**Docker job** (push to main/dev): Multi-stage build → Push to Docker Hub (tags: `<branch>`, `<branch>-<sha>`, `latest` for main)

**Deploy job** (push to main/dev): Deploy to Railway via CLI

**Required secrets:** `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `RAILWAY_TOKEN`, `RAILWAY_SERVICE_ID`

## Common Issues and Workarounds

### Build Failures

**Problem:** "release version 21 not supported"
**Solution:** Set `JAVA_HOME` to Java 21 before running Maven:
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw clean package
```

**Problem:** Missing environment variables on startup
**Solution:** Ensure `.env` file exists with all required variables (copy from `.env.example`).

**Problem:** Database schema errors (TEXT type, array types)
**Solution:** The application uses JPA auto-DDL (`hibernate.ddl-auto=update`) in dev. H2 in PostgreSQL compatibility mode may have issues with some types. For production, use actual PostgreSQL and enable Flyway migrations.

### Shell Scripts
Scripts (`build.sh`, `run.sh`, `stop.sh`, `test_api.sh`) have **hardcoded macOS paths**. In CI/Linux, use Maven Wrapper directly:
```bash
./mvnw clean package -DskipTests  # Instead of ./build.sh
./mvnw spring-boot:run            # Instead of ./run.sh
```

### Testing
No tests exist (`src/test` empty). When adding: place in `src/test/java/`, use `@SpringBootTest`, run `./mvnw test`

## Development Workflow

### Making Code Changes
1. Set Java 21 env vars → 2. Code in appropriate module → 3. Build: `./mvnw clean package -DskipTests` → 4. Test: `./mvnw spring-boot:run` → 5. Verify: Swagger (`/swagger-ui.html`), health (`/actuator/health`)

### Database Migrations
Create `src/main/resources/db/migration/V1_6__<description>.sql` (next version after existing V1_5). Enable: `FLYWAY_ENABLED=true`, `JPA_DDL_AUTO=validate`

### Code Style
2 spaces, LF endings, UTF-8 (`.editorconfig`). No linter configured.

## Quick Reference

**Root files:** `Dockerfile` (multi-stage, JDK 21), `docker-compose.yml`, `pom.xml`, Maven Wrapper, shell scripts (macOS-only), `README.md`

**Commands:**
```bash
java -version                           # Verify Java
./mvnw clean package -DskipTests        # Quick build
./mvnw spring-boot:run                  # Run with logs
docker-compose up -d                    # Docker background
```

**Key endpoints:** Health (`/actuator/health`), Swagger (`/swagger-ui.html`), Auth (`/api/auth/login`), Users (`/api/users/register/gym-owner`), Gyms (`/api/gyms/register`)

---

## Agent Instructions

**Trust these instructions.** Only search the codebase or experiment when:
1. Instructions are incomplete for your specific task
2. You find instructions are incorrect or outdated
3. You're adding a new feature not covered here

**Before coding:**
1. Set Java 21 environment (`export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64`)
2. Ensure `.env` exists
3. Build once to verify setup: `./mvnw clean package -DskipTests`

**Build time expectations:**
- Clean package (no tests): 18-30 seconds
- Tests (when they exist): Add 5-15 seconds
- First build (downloads deps): 60-90 seconds

**Always use Maven Wrapper (`./mvnw`)** instead of system Maven for reproducible builds matching CI.
