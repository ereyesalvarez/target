# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- `./gradlew build` - Compiles Kotlin with JDK 21, runs Detekt linting, and executes all tests
- `./gradlew test` - Runs JUnit 5 test suite; add `-i` for verbose output
- `./gradlew test --tests "ClassName.testMethodName"` - Run a single test method
- `./gradlew test --tests "ClassName"` - Run all tests in a specific class
- `./gradlew run` - Launches `target.MainKt` for local development
- `./gradlew shadowJar` - Produces executable fat JAR in `build/libs/`
- `./gradlew detekt` - Run Detekt linter (configuration: `config/detekt/detekt.yml`)
- `docker compose up --build` - Start Postgres service for local development

## Architecture Overview

This is an HTTP4k-based Kotlin application using hexagonal/ports-and-adapters architecture:

### Package Structure

- `src/main/kotlin/target/app/` - Domain logic layer
  - `{domain}/model/` - Domain models and value objects
  - `{domain}/port/` - Port interfaces (both "in" for use cases and "out" for persistence)
  - `{domain}/uc/` - Use cases (business logic orchestration)
  - `{domain}/command/` - Command objects for use case input
  - `{domain}/adapter/` - Inbound/outbound adapters (web DTOs, DB implementations)

- `src/main/kotlin/target/infra/` - Infrastructure layer
  - `infra/http/` - HTTP routing, DTOs, filters, exception handling
  - `infra/db/` - Database configuration (HikariCP + JDBI)
  - `infra/auth/` - JWT service and authentication routes
  - `infra/properties/` - Application configuration loaders
  - `infra/serializer/` - JSON serializers for kotlinx.serialization
  - `infra/metric/` - Prometheus metrics configuration
  - `infra/sentry/` - Sentry error tracking setup

### Application Entry Points

- `Main.kt` - Entry point that instantiates and starts `App`
- `App.kt` - Application setup: creates meter registry, JWT service, and HTTP server
- `infra/http/routes.kt` - Central routing configuration with CORS, exception handling, and auth filters

### Key Architectural Patterns

1. **Ports and Adapters**: Domain logic (`app/`) depends on port interfaces. Infrastructure (`infra/`) provides implementations.

2. **Authentication Flow**:
   - Public routes: `/login` (returns JWT)
   - Protected routes: Wrapped with `authFilter` that validates JWT and extracts userId
   - Access userId in routes via `userIdKey(request)`

3. **Database Access**: Uses JDBI with HikariCP connection pooling. Configuration optimized for single-user workloads with small pool sizes (2 max, 1 min idle).

4. **Serialization**: kotlinx.serialization with custom serializers for BigDecimal, LocalDate, and Currency types in `infra/serializer/`.

5. **Error Handling**: Global exception handler in `infra/http/exceptionHandler.kt` converts exceptions to JSON error responses.

## Testing

### Running Tests

- Unit tests: Located in `src/test/kotlin/target/`, named `*Test.kt`
- Integration tests: `src/test/kotlin/target/it/TargetAppIT.kt` tagged with `@Tag("integration")`
- Test database: Uses Testcontainers with Postgres 17, initialized from `src/test/resources/schema.sql`
- Test fixtures: Defined in `src/test/kotlin/target/fixtures/`

### Test Libraries

- JUnit 5 for test framework
- AssertK for fluent assertions
- Mockito-Kotlin for mocking
- Testcontainers for Postgres integration tests
- OkHttp for HTTP client in integration tests

### Test Configuration

- Override properties via `application-test.properties`
- System properties can override config: `System.setProperty("config.override.db.jdbc", ...)`

## Coding Standards

### Style Conventions

- 4-space indentation
- Trailing commas enabled
- PascalCase for classes
- camelCase for functions/variables
- SCREAMING_SNAKE_CASE for constants
- Max line length: 120 characters
- Test method names: sentence case (e.g., `fun returns401WhenTokenExpired()`)

### Detekt Configuration

- Enforced via CI (`./gradlew build`)
- Config: `config/detekt/detekt.yml`
- Key rules:
  - Max cyclomatic complexity: 14
  - Max method length: 60 lines
  - Max class size: 600 lines
  - Max return statements: 2
  - Max function parameters: 5 (constructors: 6)
  - Magic numbers disallowed in production code (except -1, 0, 1, 2)

### Serialization

- Use kotlinx.serialization with explicit `@Serializable` annotations
- Never modify DTO wire contracts without backward compatibility consideration
- Custom serializers available: `BigDecimalSerializer`, `LocalDateSerializer`, `CurrencyAsCodeSerializer`

## Configuration

- Properties files: `src/main/resources/application.properties`, `application-local.properties`
- Environment variables override properties
- Database config: `AppDBConfig` in `infra/properties/definition/`
- Server config: `AppServerProps` (port defaults to random 8000-9000 if not set)
- Keep secrets out of properties files - use environment variables

## Dependencies

### Core Libraries

- **HTTP4k**: HTTP server and routing
- **kotlinx.serialization**: JSON serialization
- **JDBI 3**: SQL database access
- **HikariCP**: Connection pooling
- **PostgreSQL**: Database driver
- **JJWT**: JWT token handling
- **Argon2**: Password hashing
- **Micrometer + Prometheus**: Metrics
- **Sentry**: Error tracking
- **SLF4J + Logback**: Logging

## Current Domains

### Auth Domain (`app/auth/`)

- User credential management with Argon2 password hashing
- JWT-based authentication
- Ports: `CredentialPersistencePort`, `EncryptPort`
- Use cases: `AuthService` (login), `CredentialService` (credential management)

### Asset Domain (`app/asset/`)

- Asset tracking with categories and types
- Time-series data points for assets
- Ports: `AssetPersistPort`
- Use cases: `CreateAsset`, `UpsertAssetDatapoint`, `TimeSeriesUC`
- Web routes: Asset creation, datapoint management, time-series queries

## Git Workflow

- Commit messages: Imperative mood, <60 chars, optional scope (e.g., "auth: add password rehash")
- PRs must pass `./gradlew build` (includes tests and Detekt)
- Include test evidence in PR descriptions
