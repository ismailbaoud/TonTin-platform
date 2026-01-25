# TonTin Platform – Backend Service

The TonTin Platform backend is a Spring Boot REST API that powers digital tontines (“darts”), memberships, and savings rounds. It provides authentication, profile management, dart lifecycle operations, and member orchestration with a security-first, layered architecture.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Domain Model](#domain-model)
3. [Getting Started](#getting-started)
4. [Configuration](#configuration)
5. [Usage](#usage)
   - [Authentication](#authentication)
   - [Profile Management](#profile-management)
   - [Dart Lifecycle](#dart-lifecycle)
   - [Member Operations](#member-operations)
   - [Rounds](#rounds)
6. [Testing & Quality](#testing--quality)
7. [API Documentation](#api-documentation)
8. [Security Considerations](#security-considerations)
9. [Operational Playbook](#operational-playbook)
10. [Contribution Workflow](#contribution-workflow)
11. [README Maintenance Best Practices](#readme-maintenance-best-practices)
12. [License](#license)

---

## Architecture

```
└── src/main/java/com/tontin/platform
    ├── controller/       // REST endpoints (Auth, Dart, Member, etc.)
    ├── service/          // Interfaces plus business services
    ├── service/impl/     // Concrete service implementations
    ├── domain/           // JPA entities (User, Dart, Member, Round, ...)
    ├── repository/       // Spring Data JPA repositories
    ├── dto/              // Request/response DTOs
    ├── mapper/           // MapStruct & manual mappers
    ├── config/           // Security, JWT, Jackson, OpenAPI config
    └── Exception/        // Global exception handler
```

Key characteristics:

- **Layered design** keeps transport, business rules, and persistence isolated.
- **MapStruct/manual mappers** ensure DTO consistency and prevent leaking entities.
- **JWT-based security** with `JwtAuthenticationFilter`, `JwtService`, and `SecurityUtils`.
- **Global error handling** returns normalized JSON (`ApiExceptionResponse`) for predictable clients.

---

## Domain Model

- **User**
  - Fields: `userName`, `email`, `password`, `creationDate`, `emailConfirmed`,
    `accountAccessFileCount`, `resetPasswordDate`, `role`, `picture`, `status`.
  - Relationships: One-to-one with `Member`.
- **Dart**
  - Represents a tontine/savings group. Tracks name, status, start date,
    monthly contribution, allocation method, and members.
- **Member**
  - Joins users to darts with `DartPermission` and `MemberStatus`.
- **Round**
  - Table `rounds`. Fields: `number`, `status` (`PAYED`, `INPAYED`),
    `date` (`LocalDateTime`), `amount` (`Double`), many-to-one with `Dart`.

Business rules:
- Dart creators become organizers automatically.
- Organizers can modify darts and manage members; deletion allowed only while members are `PENDING`.
- Members cannot be removed once `ACTIVE`.
- Round status helpers (`isPayed`, `isInPayed`) simplify lifecycle logic.

---

## Getting Started

### Prerequisites
- JDK 17+
- Maven Wrapper (bundled)
- PostgreSQL / compatible relational database (adapt DataSource settings as needed)
- (Optional) Docker for containerized deployment

### Build & Run

```bash
# Compile
./mvnw -q -DskipTests compile

# Unit/integration tests
./mvnw test

# Package runnable JAR
./mvnw clean package

# Run (Spring Boot)
./mvnw spring-boot:run
```

Default application configuration runs on port `8080`.

---

## Configuration

Configure sensitive values through environment variables or `application-*.yml`:

| Property                                 | Description                                                 |
|------------------------------------------|-------------------------------------------------------------|
| `spring.datasource.*`                    | JDBC connection settings                                    |
| `security.jwt.secret-key`                | Base64-encoded HS256 secret (>= 256 bits)                   |
| `security.jwt.access-token.expiration`   | Access token validity (ms)                                  |
| `security.jwt.refresh-token.expiration`  | Refresh token validity (ms)                                 |
| `security.jwt.issuer`                    | JWT issuer string                                           |
| `security.password.bcrypt.strength`      | BCrypt cost factor (default 10)                             |
| `springdoc.swagger-ui.path`              | Swagger UI path (if customized)                             |
| Mail settings (`spring.mail.*`)          | Required for verification email delivery                    |

> **Never** hardcode secrets or push them to version control. Use environment variables, Vault, or a secrets manager.

---

## Usage

### Authentication

- `POST /api/v1/auth/register` – Create account (sends verification email)
- `GET /api/v1/auth/verify?code=...` – Activate account
- `POST /api/v1/auth/login` – Acquire access/refresh tokens
- `POST /api/v1/auth/refresh-token` – Rotate access token
- `POST /api/v1/auth/logout` – Clear server-side context
- `GET /api/v1/auth/me` – Fetch current profile (secured)
- `PUT /api/v1/auth/me` – Update profile (name, password, picture)

Profile updates use JSON body:

```json
{
  "userName": "new_name",
  "password": "N3wStr0ngP@ss",
  "picture": "base64EncodedBinary..."
}
```

Back-end handles trimmed names, password hashing, `resetPasswordDate`, and picture diffs.

### Profile Management Best Practices

- Password updates trigger re-hash and reset timestamp.
- Omit fields to leave them unchanged.
- Send empty byte array (`""` base64) to clear profile pictures.

### Dart Lifecycle

Representative endpoints (secured: organizer/client roles):

- `POST /api/v1/dart` – Create (current user becomes organizer)
- `GET /api/v1/dart/{id}` – Retrieve details
- `PUT /api/v1/dart/{id}` – Update attributes
- `DELETE /api/v1/dart/{id}` – Delete (only when no active members)

Service validations convert application errors into `ResponseStatusException`.

### Member Operations

- `POST /api/v1/member/dart/{dartId}/user/{userId}` – Invite/add member (organizers only)
- `PUT /api/v1/member/{memberId}/dart/{dartId}` – Change permissions
- `GET /api/v1/member/{memberId}?dartId=...` – Fetch details
- `GET /api/v1/member/dart/{dartId}` – List members
- `DELETE /api/v1/member/{memberId}?dartId=...` – Remove pending member

Organizers enforce invariants (e.g., cannot remove last organizer; cannot delete active member).

### Rounds

Rounds are persisted via `Round` entity:
- Ensure numbers are unique per dart in your business layer.
- Use `RoundStatus` to track payment progress.
- `calculateTotalMonthlyContributions` in `Dart` helps compute pool size.

---

## Testing & Quality

Recommended commands:

```bash
# Unit tests
./mvnw test

# Clean + full verification
./mvnw clean verify

# (Optional) Format & static analysis (add SpotBugs/Checkstyle as needed)
```

CI best practices:
- Execute at least `./mvnw -q -DskipTests compile` on every PR for fast feedback.
- Run full `verify` on merge to main.
- Publish coverage and lint results when available.

---

## API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI spec**: `http://localhost:8080/v3/api-docs`

Security scheme: Bearer JWT (supply `Authorization: Bearer <token>` for protected endpoints).

---

## Security Considerations

- Restore strict authorization rules in `SecurityConfig` before production (currently permissive for testing).
- Use HTTPS and secure cookie handling in deployments.
- Rotate JWT secret regularly and store outside source control.
- Set sensible password strength and account lock policies.
- Sanitize all incoming data; rely on bean validation plus service-level guards.

---

## Operational Playbook

- **Logging**: Key service actions log at `info`/`debug`; errors at `warn`/`error` with contextual IDs.
- **Tracing**: `ApiExceptionResponse` includes `traceId` for error correlation.
- **Static resources**: Serve from CDN or separate service; backend focuses on API.
- **Backups**: Backup database frequently; consider entity versioning if concurrent edits are expected.

Deployment options:
- Spring Boot JAR with systemd
- Containerized (Docker) behind reverse proxy
- Kubernetes (define ConfigMaps/Secrets for env)

---

## Contribution Workflow

1. Create a feature branch from `main`.
2. Implement changes with tests and update documentation/README if setup changes.
3. Run `./mvnw -q -DskipTests compile` (minimum) before pushing.
4. Submit PR with:
   - Description of changes and rationale
   - Testing evidence
   - Updated docs or TODO statements for follow-up
5. Await review; address feedback promptly.
6. Squash & merge or follow repository merge policy.

Consider adding:
- `CONTRIBUTING.md` for coding standards and commit conventions.
- Issue/PR templates to streamline triage.

---

## README Maintenance Best Practices

- Keep sections current when endpoints or commands change.
- Add badges (build, coverage) once CI is active and stable.
- Include changelog links when releases are published.
- Provide quick links to docs, ADRs, or dashboards.
- Review quarterly for stale instructions or broken URLs.

---

## License

Specify your project license here (e.g., MIT, Apache 2.0). Include a `LICENSE` file at the repository root and reference it in this section.