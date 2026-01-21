# Starter (Spring Boot + Kotlin)

A production-ready Spring Boot starter built with Kotlin, PostgreSQL, Flyway, Spring Security, and JWT — designed to provide a secure, clean backend baseline.

---

## Project Overview

This repository provides a backend starter foundation with:

- Spring Boot + Kotlin (Gradle Kotlin DSL)
- PostgreSQL integration via Spring Data JPA
- Flyway-based database migrations
- Centralized configuration via typed `@ConfigurationProperties`
- Production-grade JWT security baseline
- Secure, predictable error handling

This starter focuses on secure defaults and fail-fast behavior, leaving product-specific logic to be built on top.

---

## Security Baseline (Important)

This starter includes a production-ready JWT security baseline.

### Included
- JWT parsing and validation
- Fail-closed JWT filter (invalid tokens never continue)
- Unified `401 Unauthorized` and `403 Forbidden` JSON responses
- Startup validation for critical security configuration
- No default admin credentials
- Admin seeding is opt-in and restricted to non-production profiles

### To Be Implemented By You
- Login endpoint (issue tokens)
- Refresh token rotation
- Logout / token revocation
- User registration and management

---

## Tech Stack

- Language: Kotlin 2.0.21
- Framework: Spring Boot 3.4.1
- Java: 21
- Build Tool: Gradle (Kotlin DSL)
- Database: PostgreSQL
- ORM: Spring Data JPA (Hibernate)
- Migrations: Flyway
- Security: Spring Security
- JWT: JJWT 0.12.6

---

## Requirements

- Java 21+
- PostgreSQL 12+
- Gradle (optional — wrapper included)

---

## Project Structure

```
src
├── main
│   ├── kotlin
│   │   └── com/company/starter
│   │       ├── StarterApplication.kt
│   │       ├── config
│   │       │   ├── AppProperties.kt
│   │       │   └── CorsProperties.kt
│   │       ├── security
│   │       │   └── jwt
│   │       │       ├── JwtService.kt
│   │       │       └── TokenType.kt
│   │       └── common
│   │           ├── error
│   │           └── pagination
│   └── resources
│       ├── application.yml
│       ├── application-local.yml
│       ├── application-dev.yml
│       ├── application-staging.yml
│       ├── application-prod.yml
│       └── db/migration
└── test
```

Replace `com/company/starter` with your own package name.

---

## Configuration & Profiles

The project uses profile-based configuration.

Profiles:
- local — local development
- dev — shared development
- staging — pre-production
- prod — production

Configuration files:
- `application.yml`
- `application-<profile>.yml`

---

## JWT Configuration (Fail-Fast)

JWT configuration is mandatory and validated at startup.

Required environment variables:

```
JWT_SECRET=change-me-to-a-strong-secret-32-characters-minimum
JWT_ACCESS_EXP_MINUTES=30
JWT_REFRESH_EXP_DAYS=7
```

Fail-fast rules:
- The application will not start if `JWT_SECRET` is missing
- The application will not start if `JWT_SECRET` is blank
- The application will not start if `JWT_SECRET` is shorter than 32 characters

---

## Local Setup

### 1) Create PostgreSQL Database

```
CREATE DATABASE starter_db;
CREATE USER starter_user WITH ENCRYPTED PASSWORD 'starter_pass';
GRANT ALL PRIVILEGES ON DATABASE starter_db TO starter_user;
```

---

### 2) Local Environment (.env)

Create a `.env` file (do not commit it):

```
DB_URL=jdbc:postgresql://localhost:5432/starter_db
DB_USERNAME=starter_user
DB_PASSWORD=starter_pass

JWT_SECRET=change-me-to-a-strong-secret-32-characters-minimum
JWT_ACCESS_EXP_MINUTES=30
JWT_REFRESH_EXP_DAYS=7
```

---

### 3) Run Locally

```
./gradlew bootRun --args='--spring.profiles.active=local'
```

Or via IntelliJ:
- Run Configuration → Active profiles: local

---

## Admin Seeding (Local / Dev Only)

Admin seeding is disabled by default in all environments.

To explicitly seed an admin user:

```
ADMIN_SEED_ENABLED=true
ADMIN_EMAIL=admin@local.com
ADMIN_PASSWORD=StrongPassword123!
```

Rules:
- Admin seeding works only in local and dev profiles
- No default admin credentials exist in the repository
- If credentials are missing, seeding is skipped with a warning
- Admin creation is idempotent

---

## Database Migrations (Flyway)

- Migrations are located in `src/main/resources/db/migration`
- Applied automatically at startup
- Application fails fast if a migration fails

Example:
```
V1__init.sql
V2__add_users.sql
```

---

## Error Handling

This starter includes centralized error handling:
- Consistent JSON error responses
- Standard HTTP status mapping
- Field-level validation errors
- Custom domain exceptions

---

## Build & Test

Build:
```
./gradlew clean build
```

Run tests:
```
./gradlew test
```

---

## Security Best Practices

- Use different JWT secrets per environment
- Never commit secrets to Git
- Keep access tokens short-lived
- Rotate refresh tokens if applicable
- Use HTTPS in all non-local environments

---

## Suggested Next Steps

After cloning this starter:
1. Implement authentication endpoints
2. Add user domain and persistence
3. Add role-based authorization
4. Add audit logging
5. Add health checks and metrics

---

## Contributing

- Create feature branches
- Keep pull requests small and focused
- Add tests where relevant
- Document security-related changes

---

## License

Add a license that matches your intended usage (MIT, Apache-2.0, proprietary, etc.).

---

This starter is intentionally strict.
Secure defaults and fail-fast behavior are prioritized over convenience.
