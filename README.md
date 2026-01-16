# Starter (Spring Boot + Kotlin) 🚀

A Spring Boot starter template built with Kotlin, PostgreSQL, Flyway, Spring Security, and JWT dependencies — designed to help you bootstrap backend projects quickly with a clean baseline.

## Project Overview

This repository is a **backend starter** built with Spring Boot and Kotlin. It provides a solid foundation for:
- Standard Gradle Kotlin DSL project setup
- PostgreSQL integration via Spring Data JPA
- Flyway-based schema migrations (database-first approach)
- `.env` loading via `spring-dotenv`
- Centralized JWT configuration through typed `@ConfigurationProperties` (`app.jwt.*`)

> **Note:** JWT + Security dependencies are included, but the full authentication flow (controllers/filters/token issuing) is meant to be implemented on top of this starter according to your needs.

## Tech Stack

- **Language**: Kotlin 2.0.21
- **Framework**: Spring Boot 3.4.1
- **Java**: 21 (toolchain)
- **Build Tool**: Gradle (Kotlin DSL)
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Migrations**: Flyway
- **Security**: Spring Security
- **JWT Library**: JJWT 0.12.6
- **.env Support**: `me.paulschwarz:spring-dotenv` 4.0.0

## Requirements

- **Java 21** (JDK 21+)
- **PostgreSQL** (recommended 12+)
- **Gradle** (optional — the project includes `./gradlew`)

## Project Structure

```text
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
└── src
    ├── main
    │   ├── kotlin
    │   │   └── com/company/starter
    │   │       ├── StarterApplication.kt
    │   │       ├── config
    │   │       │   └── AppProperties.kt
    │   │       ├── security
    │   │       │   └── jwt
    │   │       │       ├── JwtService.kt
    │   │       │       └── TokenType.kt
    │   │       └── common
    │   │           ├── error
    │   │           │   ├── ErrorCode.kt
    │   │           │   ├── ErrorResponse.kt
    │   │           │   ├── FieldErrorResponse.kt
    │   │           │   ├── GlobalExceptionHandler.kt
    │   │           │   └── exceptions
    │   │           │       ├── BadRequestException.kt
    │   │           │       ├── ConflictException.kt
    │   │           │       ├── ForbiddenException.kt
    │   │           │       ├── NotFoundException.kt
    │   │           │       └── UnauthorizedException.kt
    │   │           └── pagination
    │   │               └── PaginationResponse.kt
    │   └── resources
    │       ├── application.yml
    │       ├── application-local.yml
    │       ├── application-dev.yml
    │       ├── application-staging.yml
    │       ├── application-prod.yml
    │       └── db
    │           └── migration
    │               └── V1__init.sql
    └── test
        └── kotlin
            └── com/company/starter
                └── StarterApplicationTests.kt
   
```
> The package path (`com/company/starter`) is an example. Replace it with your actual package name.

## Local Setup

### 1) Create PostgreSQL Database

Create a database and user (example):

```sql
CREATE DATABASE starter_db;
CREATE USER starter_user WITH ENCRYPTED PASSWORD 'starter_pass';
GRANT ALL PRIVILEGES ON DATABASE starter_db TO starter_user;
```

### 2) Configure Environment Variables (.env)

This project uses **spring-dotenv** to load environment variables from a `.env` file.

Create a `.env` file in the project root:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=starter_db
DB_USERNAME=starter_user
DB_PASSWORD=starter_pass

# JWT
JWT_SECRET=change-me-to-a-strong-secret
JWT_ACCESS_EXP_MIN=30
JWT_REFRESH_EXP_DAYS=7
```
> Keep `.env` out of Git (recommended). Add it to `.gitignore`.

### 3) Spring Profiles

The project uses profile-based configurations:

- `application.yml` (shared defaults)
- `application-local.yml`
- `application-dev.yml`
- `application-staging.yml`
- `application-prod.yml`

Run with a profile (example: **local**):

    ./gradlew bootRun --args='--spring.profiles.active=local'

Or via IntelliJ:
- Run Configuration → **Active profiles**: `local`

## Configuration (App Properties)

JWT settings are centralized under the `app` prefix (typed config via `@ConfigurationProperties`).

Typical keys:

    app:
      jwt:
        secret: ${JWT_SECRET}
        access-expiration-minutes: ${JWT_ACCESS_EXP_MIN}
        refresh-expiration-days: ${JWT_REFRESH_EXP_DAYS}

> Exact key names depend on `AppProperties.kt`. The intent is:
> - one place for JWT config
> - profile-aware overrides if needed

## Build & Run

### Build

    ./gradlew clean build

### Run (Default)

    ./gradlew bootRun

### Run with Profile (Recommended)

    ./gradlew bootRun --args='--spring.profiles.active=local'

### Run Tests

    ./gradlew test

## Database & Flyway Migrations

This project uses **Flyway** to manage database schema changes.

### Where migrations live

Migration scripts are located in:

    src/main/resources/db/migration

Example naming:

    V1__init.sql
    V2__add_users_table.sql

### How migrations run

- On application startup, Flyway automatically checks the database schema history table
- Any new migration scripts will be applied in version order
- If a migration fails, the application will stop (fail-fast)

### Tips

- Do not edit an already-applied migration in a shared environment
- Create a new migration version instead (e.g., `V3__...sql`)
- Keep migrations small and focused  

## Security & JWT Notes

Spring Security and JJWT dependencies are included to give you a solid starting point for authentication.

### JWT Configuration

JWT settings are configured via `app.jwt.*` and are typically loaded from environment variables.

Common configuration intent:

- `app.jwt.secret` → signing key (keep it strong and private)
- `app.jwt.access-expiration-*` → access token lifetime
- `app.jwt.refresh-expiration-*` → refresh token lifetime

### Recommended Practices

- Use different secrets per environment (local/dev/staging/prod)
- Never commit secrets to Git
- Keep access tokens short-lived and rely on refresh tokens for session continuity
- If you deploy behind HTTPS (recommended), consider storing refresh tokens more securely (e.g., HttpOnly cookies)

> This starter includes the dependencies and configuration baseline. The full auth flow (controllers, filters, token issuing/rotation) can be implemented on top of it based on your product needs.

## Error Handling

This starter includes a centralized error handling layer to keep API responses consistent and predictable.

### Global Exception Handling

A `@RestControllerAdvice` handles:
- common HTTP errors (bad request, unauthorized, forbidden, not found, conflict)
- validation errors
- custom domain exceptions

### Error Response Model

The project provides standard DTOs for error responses (e.g., error code + message) and field-level validation details when applicable.

### Why this matters

- consistent client experience (frontend/mobile)
- easier debugging and logging
- less duplicated try/catch logic in controllers
## Configuration & Profiles

This project uses multiple Spring profiles to manage environment-specific configuration.

### Profiles

- `local` → local development on your machine
- `dev` → shared development environment
- `staging` → pre-production testing
- `prod` → production

### Configuration Files

- `src/main/resources/application.yml` → shared defaults
- `src/main/resources/application-<profile>.yml` → profile overrides

### dotenv

With `spring-dotenv`, a `.env` file at the project root can provide environment variables locally without exporting them in your shell.

Best practice:
- use `.env` for local only
- use your deployment platform secrets manager for dev/staging/prod

## Next Steps (Suggested Roadmap)

After cloning this starter, you can implement:

1) **Authentication Module**
    - login endpoint (issue access/refresh tokens)
    - refresh endpoint (rotate tokens)
    - logout endpoint (invalidate refresh token if you store it)

2) **User Module**
    - user entity + repository
    - registration endpoint
    - user profile endpoints

3) **Security Hardening**
    - request validation + rate limiting (if needed)
    - method-level authorization (`@PreAuthorize`)
    - audit logging for auth-sensitive operations

4) **Observability**
    - structured logging
    - health checks / readiness probes
    - metrics & tracing (optional)

This starter is intentionally minimal: it gives you a clean baseline and leaves product-specific decisions to you.

## Contributing

Contributions are welcome. Suggested workflow:

- Create a feature branch
- Keep changes small and focused
- Add/adjust tests where applicable
- Open a pull request with a clear description

## License

Add a license that matches your intended usage (e.g., MIT, Apache-2.0, proprietary).

If you choose MIT, add:
- `LICENSE` file at the repository root
- a short copyright notice

