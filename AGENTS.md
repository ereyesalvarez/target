# Repository Guidelines

## Project Structure & Module Organization

`src/main/kotlin/target` hosts Kotlin sources. The `app` package keeps domain logic (e.g., `app/auth/{model,port,uc}`
for use cases and ports) while `infra` implements adapters such as HTTP routing, DTOs, metrics, and persistence (
`infra/http`, `infra/db`). Entry points sit in `Main.kt`/`App.kt`. Shared configuration lives under
`src/main/resources` (application properties, logging), and test fixtures mirror the tree under `src/test`. Static
config for linting resides in `config/detekt/detekt.yml`. Docker and Compose files in the repo root define containerized
builds and local dependencies.

## Build, Test & Development Commands

- `./gradlew build`: compiles Kotlin with JDK 21, runs Detekt, and executes all tests.
- `./gradlew test`: runs the JUnit 5 suite; combine with `-i` for verbose logs.
- `./gradlew run`: launches `target.MainKt`, useful for quick local smoke tests.
- `./gradlew shadowJar`: produces an executable fat JAR in `build/libs/`.
- `docker compose -f compose.yaml up --build`: spins up any declared services (e.g., Postgres) for running the
  application in local; stop with `docker compose down`.
-

## Coding Style & Naming Conventions

Follow the Kotlin style guide with 4-space indentation and trailing commas enabled where allowed. Use `PascalCase` for
classes, `camelCase` for functions/vars, and `SCREAMING_SNAKE_CASE` for constants. Keep package boundaries aligned with
architecture layers (`app`, `infra`). Before pushing, run `./gradlew detekt` to apply the shared lint rules defined in
`config/detekt/detekt.yml`; Detekt failures block CI. Serialization uses kotlinx serialization—annotate DTOs explicitly
to keep wire contracts stable.

## Testing Guidelines

Write unit tests beside the feature under `src/test/kotlin`, naming files `SomethingTest.kt` and test methods in
sentence case (`fun returns401WhenTokenExpired()`). Leverage AssertK for fluent assertions, Mockito-Kotlin for doubles,
and Testcontainers/Postgres for DB interactions. Keep tests hermetic.

E2E test should be done in the `src/test/kotlin/target/it/TargetAppIT.kt` Seed schemas via 
`src/test/resources/schema.sql` and load `application-test.properties` for overrides. 

Aim to cover new branches and edge cases; pull requests touching production code should include or update tests.

## Commit & Pull Request Guidelines

Commits follow the existing history: a short (<60 char) imperative summary plus optional scope, e.g.,
`auth: add password rehash`. Reference issues in the body when relevant. For PRs, describe the problem, the solution,
and testing evidence (commands run, screenshots for HTTP changes). Link related issues, call out breaking changes, and
ensure CI (`./gradlew build`) passes before requesting review.

## Security & Configuration Tips

Keep secrets and credentials out of `application*.properties`; inject via environment variables or Docker Compose
overrides. Micrometer/Prometheus and Sentry are enabled—verify new endpoints export metrics and wrap risky sections with
Sentry breadcrumbs when appropriate. When adding storage or network clients, expose configuration knobs through the
`infra.properties` loaders to keep deployments configurable.
