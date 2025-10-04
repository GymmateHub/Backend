# GymMate Backend (Spring Boot Modular Monolith)

GymMate is a modular monolith built with Spring Boot, designed to provide a comprehensive gym management system. It organizes features into cohesive modules while maintaining a single deployable unit for simplicity and speed during MVP development.

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
   git clone https://github.com/yourusername/gymmate-backend.git
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

## Key Features

- **User Management**: Registration, authentication, and profile management
- **Gym Management**: Gym registration and management
- **Membership Systems**: Membership plans and subscriptions
- **Booking System**: Class and session scheduling
- **Payment Processing**: Handle payments and subscriptions
- **Inventory Management**: Track gym equipment and resources
- **Analytics**: Business insights and reporting
- **Notification System**: Email and push notifications

## Database Migrations

The project uses Flyway for database migrations. Migration files are located in:
```
src/main/resources/db/migration/
```

## Contributing

1. Create a feature branch (`git checkout -b feature/amazing-feature`)
2. Commit your changes (`git commit -m 'Add amazing feature'`)
3. Push to the branch (`git push origin feature/amazing-feature`)
4. Open a Pull Request

## Project Status

Currently in active development. Version 1.0.0 development in progress.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
