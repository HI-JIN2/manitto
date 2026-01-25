# Agent Guide (backend)
This repository is a Spring Boot 3 + Kotlin (JDK 17) API, built with Gradle.
Primary module: `:app`.

If you are an agentic coding tool operating here, optimize for:
- Keeping controllers thin, logic in services, persistence in repositories.
- Returning consistent API errors via `CustomException` + `GlobalExceptionHandler`.
- Running the smallest-possible test scope locally before shipping.

## Commands (Build / Run / Test)
All Gradle commands should be run via the wrapper from repo root.

Build:
- Full build (includes tests): `./gradlew :app:build`
- Boot jar (what Docker/CI uses): `./gradlew :app:bootJar`
- Clean build: `./gradlew clean :app:build`

Run locally:
- Start app: `./gradlew :app:bootRun`
- Local DB (Postgres in Docker): `docker compose -f docker-compose.local.yml up -d`

Tests:
- All tests (module): `./gradlew :app:test`

Run a single test:
- One test class:
  - `./gradlew :app:test --tests 'party.manitto.domain.party.PartyServiceTest'`
- One test method (pattern match):
  - `./gradlew :app:test --tests 'party.manitto.domain.party.PartyServiceTest.createParty*'`
  - Tip: many tests use Kotlin backtick names; prefer a prefix wildcard like `createParty*`.

Useful test flags:
- Show logs: `./gradlew :app:test --info`
- CI parity: `./gradlew :app:test --no-daemon` (add `--continue` to keep going)

Lint / formatting:
- No dedicated Kotlin linter (ktlint/detekt/spotless) is configured; use IDE formatting.
- Also useful: `./gradlew :app:check` (Gradle "check" lifecycle; currently mainly tests).

Docker:
- Build image locally: `docker build -t manitto-backend:dev .`
- Run prod-like container (expects `.env`): `docker compose up -d --build`

CI parity (GitHub Actions):
- CI runs: `./gradlew :app:test --no-daemon` (see `.github/workflows/ci.yml`).

## Repo Layout
- Application module: `app/`
- Source: `app/src/main/java/party/manitto/...`
- Tests: `app/src/test/java/...`
- Config: `app/src/main/java/party/manitto/config/...`
- Shared error handling: `app/src/main/java/party/manitto/global/...`
- Flyway migrations: `app/src/main/resources/db/migration/`

## Environment / Config
- `.env` is supported via `spring-dotenv` (see `app/build.gradle.kts`).
- Start from `env.example` and copy to `.env`.
- Do not commit secrets (`.env`, credentials, tokens).

## Code Style Guidelines (Kotlin + Spring)
### Formatting
- Indentation: 4 spaces.
- Use trailing commas in multi-line parameter lists where it improves diffs.
- Prefer line length around ~120; wrap long argument lists and fluent chains.
 - Keep blank lines between `package`/`import`/declarations and between logical sections in long methods.

### Imports
- Prefer explicit imports.
- Avoid wildcard imports (e.g. `org.springframework.web.bind.annotation.*`) unless it meaningfully reduces noise.
- Import order: stdlib -> third-party (Spring/Jakarta/etc) -> project (`party.manitto...`).

### Naming
- Packages: lowercase, dot-separated (matches `party.manitto...`).
- Classes / enums: `UpperCamelCase`.
- Functions / properties: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Tests: descriptive names; backticks OK; keep an ASCII prefix so `--tests ...prefix*` is usable.

### Types / Nullability
- Prefer `val` over `var`.
- Avoid `!!`; prefer `x ?: throw CustomException(...)` and `require(...) { "..." }`.
- Model optional inputs as nullable (`String?`) and normalize early.

### Spring Conventions
- Controllers: thin; use `@Valid`; return DTOs; use `ResponseEntity` only when you need headers/status.
- Services: hold business logic; use `@Transactional` for writes; keep side effects explicit/testable.
- Repositories: Spring Data JPA only; keep query methods declarative.

### DTOs and Mapping
- Do not expose JPA entities from controllers.
- Prefer `data class` DTOs.
- Use companion mappers where already established:
  - `PartyResponse.from(entity)`
  - `ParticipantResponse.from(entity)`

### Error Handling

- Preferred: throw `CustomException(ErrorCode.X)` for business errors.
- `GlobalExceptionHandler` maps:
  - `CustomException` -> `ErrorResponse` with the `ErrorCode` status/message.
  - Validation errors (`@Valid`) -> `ErrorCode.INVALID_INPUT_VALUE` + field details.
  - `IllegalArgumentException` -> `INVALID_INPUT_VALUE` while preserving message.

Guidelines:
- Prefer `CustomException` over `IllegalArgumentException` for domain-level failures.
- Keep user-facing messages safe (no secrets/stack traces).
- Avoid `printStackTrace()` in new code; use SLF4J logging.

### Security

- Auth uses JWT in `Authorization: Bearer <token>`.
- The current user is injected as `@AuthenticationPrincipal user: User`.
- If you add new endpoints, ensure `SecurityConfig` matches intended access:
  - `permitAll()` for public endpoints
  - otherwise require authentication

### Database / Migrations

- Flyway migrations live in `app/src/main/resources/db/migration/`.
- Naming: `V<NUMBER>__<snake_case_description>.sql`.
- Keep migrations additive and backward-compatible when possible.

## Testing Guidelines

- Test framework: JUnit 5 (`useJUnitPlatform()` in Gradle).
- Mocking: MockK (`@ExtendWith(MockKExtension::class)`, `@MockK`, `@InjectMockKs`).
- MVC tests:
  - `@WebMvcTest` + `springmockk` (`@MockkBean`) + `MockMvc` Kotlin DSL.
  - If security config causes pain, mirror the existing pattern of excluding `SecurityConfig`.

When adding tests:
- Prefer unit tests for pure business rules (services).
- Prefer WebMvc tests for validation/serialization/controller wiring.
- Assert on both happy path and one key failure path.

## Cursor / Copilot Rules

- No Cursor rules found (`.cursor/rules/` or `.cursorrules` not present).
- No GitHub Copilot instructions found (`.github/copilot-instructions.md` not present).
