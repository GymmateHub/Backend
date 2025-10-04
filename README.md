# GymMate Backend (Spring Boot Modular Monolith)

GymMate is a modular monolith built with Spring Boot. It organizes features into cohesive modules while keeping a single deployable unit for simplicity and speed during MVP development.

- Java: 21
- Spring Boot: 3.3.x
- Build: Maven

Key modules:
- shared: cross-cutting concerns (config, exception, security, util, dto)
- membership, booking, user, payment, inventory, analytics, notification: each split into api, application, domain, infrastructure

Getting started
- Ensure you have JDK 21 and Maven (or use the Maven Wrapper once added)
- Build: mvn clean package
- Run: mvn spring-boot:run
