# TonTin Platform – Engineering Best Practices Reference

This document summarizes the engineering practices already in place within the project and highlights complementary practices every production-grade service should adopt. Use it as a checklist when you evolve this codebase or start a new one.

---

## 1. Dependency Management & Build Hygiene
- **Single source of truth**: All Java dependencies live in `pom.xml`; avoid manual JARs or duplicate build systems.
- **Immutable builds**: Rely on versioned dependencies; prefer ranges only when absolutely necessary.
- **Fail-fast build**: `mvnw -q -DskipTests compile` runs cleanly; integrate this command in CI to guard against accidental regressions.

## 2. Service & Business Logic Organization
- **Layered architecture**: `controller` → `service` → `repository` separation keeps HTTP concerns, business rules, and persistence isolated.
- **Interface-driven contracts**: Services (e.g., `MemberService`) expose an interface; concrete implementations are hidden behind dependency injection, which eases testing.
- **Transaction boundaries**: Annotate mutating service methods (`@Transactional`) to guarantee atomic persistence and avoid partial updates.

## 3. Domain Modeling
- **Rich entities**: JPA entities encapsulate business invariants and helper methods (e.g., `Dart#addMember`, `Member#isOrganizer`).
- **Validation annotations**: Bean validation (`@NotBlank`, `@DecimalMin`) enforce data integrity at both API and persistence boundaries.
- **Enum semantics**: Domain enums (`DartStatus`, `MemberStatus`) clearly model state machines instead of magic strings.

## 4. Request / Response DTOs
- **Records for immutability**: Request/response records capture API contracts and avoid accidental mutation.
- **Self-validation helpers**: Methods like `DartRequest#isValidAllocationMethod()` centralize validation logic instead of scattering checks.
- **Mapper layer**: MapStruct or hand-written mappers translate between entities and DTOs, preventing controllers from leaking persistence internals.

## 5. Security & Authentication
- **JWT-based stateless auth**: `JwtAuthenticationFilter` parses and validates tokens; `JwtService` issues access/refresh tokens with expiration and type claims.
- **Custom user identity**: `CustomUserDetailsService` loads domain users; `CustomUserDetails` adapts them to Spring Security.
- **Security utilities**: `SecurityUtils` exposes minimal helpers (`requireCurrentUserId`, `requireCurrentUser`) for service layers without making them security-aware.

> _Note_: The current `SecurityConfig` is intentionally permissive for testing. Restore role-based `authorizeHttpRequests` rules before deploying.

## 6. Authorization & Ownership Rules
- **Organizer enforcement**: Member operations require the acting user to be an organizer; checks happen in `MemberServiceImpl`.
- **Pending-state invariants**: Members can be deleted only while `PENDING`; business rules raise `ResponseStatusException` with 409 Conflict otherwise.
- **Consistent organizer creation**: `DartServiceImpl` sets the authenticated user as organizer through `memberService.createMember`.

## 7. Error Handling & Observability
- **Centralized exception handler**: `GlobalExceptionHandler` maps `ResponseStatusException`, validation errors, security exceptions, and unexpected failures to structured JSON (`ApiExceptionResponse`).
- **Traceability**: Each error response includes a `traceId` to correlate logs with client-visible errors.
- **Granular logging**: Services log intent (`info`) and lifecycle events (`debug`), while error paths log warnings/errors with contextual details.

## 8. Validation & Guard Rails
- **Request validation**: `@Valid` on controller methods ensures DTO constraints are honored before reaching services.
- **UUID sanity checks**: Helpers verify input identifiers are not null before hitting repositories, returning 400 with descriptive messages.
- **Conflict detection**: Repository helpers (e.g., `existsByUserIdAndDartId`) guard against duplicate memberships or removal of the last organizer.

## 9. Mapper & Repository Discipline
- **Targeted queries only**: `MemberRepository` exposes just the predicates the services need; unused methods are removed to limit API drift.
- **Manual mappers when necessary**: `MemberMapper` intentionally avoids MapStruct to fine-tune nested DTO construction.
- **No leaking lazy proxies**: DTO mapping occurs inside the transactional boundary to avoid LazyInitializationExceptions.

## 10. Configuration Management
- **Central JSON config**: `JacksonConfig` registers the Java Time module and disables timestamp serialization to keep API payloads consistent.
- **Password policy**: `PasswordConfig` externalizes BCrypt strength (`security.password.bcrypt.strength`) so environments can harden hashing cost.
- **OpenAPI metadata**: `OpenApiConfig` documents the API with bearer security scheme, providing ready-to-use Swagger UI integration.

## 11. Testing & Automation (to strengthen further)
- **Unit & integration coverage** *(future work)*: Add tests for service invariants, security edge cases, and repository behavior.
- **Mockless service tests**: With interface- and DI-friendly design, integrate test slices (e.g., `@DataJpaTest`, `@WebMvcTest`) for targeted verification.
- **Continuous integration**: Configure a pipeline that runs compile, tests, and static analysis (SpotBugs/Checkstyle) on every merge request.

## 12. Documentation & Onboarding
- **This README**: Serves as a living checklist; update it as new practices are adopted.
- **API docs**: Swagger UI already available; document authentication steps and sample JWT payloads.
- **Coding standards**: Favor logs over comments for run-time insight, keep classes under responsibility, and enforce consistent naming.

---

### Quick Checklist for New Projects
1. **Set up security skeleton**: JWT issuing, filter, exception entrypoint.
2. **Define service interfaces** and wire them with DI.
3. **Centralize error handling** with a structured response object.
4. **Use DTOs + mappers**, never expose entities directly.
5. **Guard every business rule** with explicit status checks and repository helpers.
6. **Log intentionally**, include IDs and states.
7. **Automate builds/tests** with Maven wrapper in CI.
8. **Document practices** so future contributors stay aligned.

By iterating on this list, every new service stays clean, auditable, and production-ready from day one.