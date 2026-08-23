# GymMateHub Backend (Spring Boot Modular Monolith)

GymMateHub is a modular monolith built with Spring Boot, designed to provide a comprehensive gym management system. It organizes features into cohesive modules while maintaining a single deployable unit for simplicity and speed during MVP development.

## 📢 Recent Updates

### ✅ November 20, 2025 - Tenant Context Fix
- **Fixed**: GET `/api/gyms` endpoint now works correctly (was returning 403 Forbidden)
- **Updated**: SecurityConfig to properly handle public gym listing endpoints
- **Enhanced**: TenantFilter for better public endpoint handling

👉 **For technical details, testing, and implementation gaps**: See `TECHNICAL_NOTES.md`

## Tech Stack

- **Java**: 21
- **Spring Boot**: 3.3.x
- **Build Tool**: Maven
- **Database**: PostgreSQL (with Flyway for migrations)
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Architecture**: Modular Monolith with Clean/Hexagonal Architecture principles

## Project Structure

The project follows a modular monolithic architecture with clear separation of concerns:

```
src/main/java/com/gymmate/
├── shared/                 # Cross-cutting concerns
│   ├── config/            # Application configuration
│   ├── dto/               # Common DTOs
│   ├── exception/         # Global exception handling
│   ├── security/         # Security configuration
│   ├── service/          # Shared services
│   └── util/             # Utility classes
│
├── user/                  # User Management Module
├── membership/           # Gym & Membership Module
├── booking/             # Class & Session Booking
├── payment/             # Payment Processing
├── inventory/           # Equipment & Resource Management
├── analytics/           # Business Analytics
└── notification/        # Notification System
```

Each feature module follows a clean architecture pattern with four layers:
- **api**: Controllers, DTOs, and API endpoints
- **application**: Services and use cases
- **domain**: Core business logic and entities
- **infrastructure**: External implementations (repositories, adapters)

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.8+
- PostgreSQL 15+

### Local Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/GymMateHub/backend.git
   cd gymmate-backend
   ```

2. Configure your database in `src/main/resources/application.yml`

3. Build the project:
   ```bash
   ./mvnw clean package
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Available Scripts

- `build.sh`: Builds the application
- `run.sh`: Starts the application
- `stop.sh`: Stops the running application
- `test_api.sh`: Runs API tests

## Documentation

### Technical Documentation
📘 **[TECHNICAL_NOTES.md](docs/TECHNICAL_NOTES.md)** - **Start here for technical details**
- Recent bug fixes and changes
- Known issues and solutions
- Implementation gaps and roadmap
- Testing guidelines
- Technical decisions
- API documentation
- Development troubleshooting

### Business Documentation
- 📋 **[BRD](docs/gymmate_brd.md)** - Business Requirements Document
- 🗂️ **[Schema](docs/gymmate_schema.md)** - Database schema
- 📖 **[Comprehensive Spec](docs/gymmate_comprehensive_spec.md)** - Detailed specifications

## Key Features

- **User Management**: Registration, authentication, and profile management
- **Gym Management**: Gym registration and management
- **Membership Systems**: Membership plans and subscriptions
- **Booking System**: Class and session scheduling
- **Payment Processing**: Handle payments and subscriptions
- **Inventory Management**: Track gym equipment and resources
- **Analytics**: Business insights and reporting
- **Notification System**: Email and push notifications

## Database Migrations & Pre-Go-Live Cleanup

### Migrations
The project uses Flyway for database migrations. Migration files are located in:
```
src/main/resources/db/migration/
```

### 🧹 Pre-Go-Live Test Data Cleanup
Before launching to production, any test data (test organizations, gyms, dummy members, test payments, fake workouts, and mock access logs) must be purged from the PostgreSQL database.

A safe, transactional cleanup script is provided at:
- **SQL Script**: [`scripts/cleanup_test_data_before_golive.sql`](scripts/cleanup_test_data_before_golive.sql)
- **Runner Script**: [`scripts/cleanup_vps_db.sh`](scripts/cleanup_vps_db.sh)

**What the cleanup script does:**
1. **Preserves Schema & Migrations**: Does NOT drop tables or touch `flyway_schema_history`.
2. **Preserves System Seed Catalogs**: Retains platform `subscription_tiers` (Starter, Professional, Enterprise) and system `exercise_categories`.
3. **Safely Truncates Test Data**: Cleans all tenant, user, transactional, and operational records inside an atomic transaction.
4. **Auto-initializes Super Admin**: When the backend container restarts post-cleanup, `SuperAdminInitializer` automatically provisions the primary Super Admin account defined in your environment variables (`APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`).

**How to run on the VPS:**
```bash
# Option 1: Using the interactive bash runner
chmod +x scripts/cleanup_vps_db.sh
./scripts/cleanup_vps_db.sh

# Option 2: Running via Docker command directly
docker exec -i $(docker ps -q -f name=postgres) psql -U gymmate -d gymmate < scripts/cleanup_test_data_before_golive.sql
docker compose restart gymmate-backend
```

## Email Deliverability (Mailtrap Live SMTP)

The backend is configured to use **Mailtrap Live SMTP** as its default transactional email provider:
- **Host**: `live.smtp.mailtrap.io`
- **Port**: `587` (STARTTLS)
- **Username**: `api`
- **Password**: `<YOUR_MAILTRAP_LIVE_API_TOKEN>`
- **From Address**: Must use a domain verified in your Mailtrap Sending Domains dashboard (e.g. `noreply@gymmatehub.com` or `noreply@vantroxialabs.com`).


## Contributing

1. Create a feature branch (`git checkout -b feature/amazing-feature`)
2. Commit your changes (`git commit -m 'Add amazing feature'`)
3. Push to the branch (`git push origin feature/amazing-feature`)
4. Open a Pull Request

## Project Status

Currently in active development. Version 1.0.0 development in progress.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
