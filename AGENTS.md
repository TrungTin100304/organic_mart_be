# AGENTS.md

> Universal instructions for AI coding agents working on `organic_mart_be`.
> Tool-specific overrides live in `CLAUDE.md` (Claude Code) and `.github/copilot-instructions.md` (GitHub Copilot).
> If you are an agent and find conflicting guidance, the tool-specific file wins for that tool.

---

## Project

Spring Boot 4 / Java 21 backend monolith for **organic-mart**.

- Stateless REST API with JWT authentication
- PostgreSQL + Flyway for schema migration
- JPA / Hibernate for persistence
- MapStruct for DTO mapping (compile-time, type-safe)
- Lombok for boilerplate (entities use limited Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`; never `@Data`)
- Deployed as a Docker container

Base package is `com.bryan` (see `src/main/java/com/bryan/OrganicMartPeApplication.java`).

Detailed stack with versions: `docs/STACK.md`.

---

## Read these before any change

| Document | Purpose |
|---|---|
| `docs/ ARCHITECTURE.md` | Layer responsibilities, request lifecycle, module layout (note the leading space in filename) |
| `docs/CONVENTIONS.md` | Naming, package structure, Lombok / MapStruct rules |
| `docs/STACK.md` | Exact versions, libraries, why each was chosen |
| `docs/patterns/jpa-patterns.md` | **Critical** — JPA / Hibernate pitfalls and fixes |
| `docs/patterns/api-design.md` | REST API design and HTTP status code rules |
| `docs/patterns/spring-layer-pattern.md` | Layer-by-layer templates for new features |

---

## Build & verify commands

You **must** run these in order and confirm they pass before marking a task done:

```bash
./mvnw compile -q          # 1. Verify it compiles
./mvnw test                # 2. Run all tests
./mvnw clean verify        # 3. Full build with quality checks
```

On Windows PowerShell, use the wrapper cmd script:

```powershell
.\mvnw.cmd compile -q
.\mvnw.cmd test
.\mvnw.cmd clean verify
```

Run a single test:
```bash
./mvnw test -Dtest=AuthServiceImplTest
./mvnw test -Dtest=AuthServiceImplTest#login_success
```

Run locally for smoke test:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# Swagger UI → http://localhost:8080/swagger-ui.html
```

If compile fails → check MapStruct mappings, Lombok `final` fields, circular deps.
If tests fail → check Mockito mock setup, JWT/security-context assumptions, and mail sender stubs.

---

## Hard constraints (NEVER violate)

- **NEVER** modify an existing Flyway migration in `src/main/resources/db/migration/`. Always add a new `V{n+1}__{description}.sql`.
- **NEVER** commit `.env`, `application-local.yml`, or any file with secrets.
- **NEVER** use `@Data` on JPA entities — it generates `toString()` that triggers lazy loads and `hashCode()` that breaks `Set` semantics.
- **NEVER** return raw `@Entity` types from controllers. Always map to a Response DTO.
- **NEVER** set `FetchType.EAGER` globally on `@ManyToOne` / `@OneToMany`. Use `JOIN FETCH` or `@EntityGraph` per query.
- **NEVER** put business logic in controllers. Controllers are thin.
- **NEVER** return `null` from a service to indicate "not found". Throw a typed exception.
- **NEVER** call `repository.save()` on an already-managed entity inside `@Transactional` — dirty checking handles it.
- **NEVER** add a dependency to `pom.xml` without explicit user approval.

---

## Architecture rule

Dependency flow is **one-way**:

```
controller → service (interface) → service/impl → repository → entity
```

A class in `entity` never imports from `controller` or `service`.
A `repository` interface never contains business logic.
A `controller` never injects a `Repository` directly.

Reference structure in code: `src/main/java/com/bryan/controller`, `src/main/java/com/bryan/service`, `src/main/java/com/bryan/service/impl`, `src/main/java/com/bryan/repository`, `src/main/java/com/bryan/entity`.

Detailed layer responsibilities: `docs/ ARCHITECTURE.md`.

---

## Naming conventions

| Type | Pattern | Example |
|---|---|---|
| Entity | `{Noun}` | `User`, `Order` |
| Repository | `{Noun}Repository` | `UserRepository` |
| Service interface | `{Noun}Service` | `UserService` |
| Service impl | `{Noun}ServiceImpl` | `UserServiceImpl` |
| Controller | `{Noun}Controller` | `UserController` |
| Request DTO | `{Action}{Noun}Request` | `CreateUserRequest` |
| Response DTO | `{Noun}Response` | `UserResponse` |
| Mapper | `{Noun}Mapper` | `UserMapper` |
| Exception | `{Description}Exception` | `ResourceNotFoundException`, `BadRequestException` |

DTOs are Java **records**. Request DTOs have Bean Validation annotations. Response DTOs do not.

---

## API response contract

Controllers return `ResponseEntity<ApiResponse<T>>` via static helpers in `src/main/java/com/bryan/dto/response/ApiResponse.java`.

```json
// Success
{ "status": 200, "message": "Success", "data": { ... } }

// Paginated list
{ "status": 200, "message": "Success", "data": { "content": [...], "page": 0, "size": 20, "totalElements": 143, "totalPages": 8, "last": false } }

// Error
{ "status": 404, "message": "User with id 42 not found", "data": null }
```

HTTP status codes:

| Scenario | Status |
|---|---|
| Created | `201 Created` |
| Read / list / update | `200 OK` |
| Delete | `204 No Content` |
| Validation failure | `400 Bad Request` |
| Missing or invalid JWT | `401 Unauthorized` |
| Authenticated but forbidden | `403 Forbidden` |
| Resource not found | `404 Not Found` |
| Business rule violation | `409 Conflict` |
| Server error | `500 Internal Server Error` |

Full rules: `docs/patterns/api-design.md`.

---

## Workflow — Spec-Driven Development (SDD)

Features go through three stages:

1. **Spec** — define what to build. Output: `spec/{feature}.md`
2. **Plan** — design how to build it. Output: `plan/{feature}.md`
3. **Implement** — write code from spec + plan. Verify build + tests.

Implementation order (respects compile dependencies):

```
entity → migration → repository → service interface → service impl
  → DTOs → mapper → controller → exception → test
```

After implementation, the task is **not done** until `./mvnw clean verify` passes.

Claude Code users: `.claude/skills/kn-spec`, `kn-plan`, `kn-implement`.
Copilot Agent users: `/spec-feature`, `/plan-feature`, `/implement-feature` prompt commands.

---

## Git conventions

- Branch: `feature/{ticket}-short-description` or `fix/{ticket}-short-description`
- Commit format (Conventional Commits): `feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`
- Example: `feat: add user registration endpoint (#42)`
- Never push directly to `main` or `develop`
- All tests must pass in CI before merge
- PR description must include: what changed, why, how to test, migration notes (if any)

---

## Definition of done

A task is **not complete** until every box is checked:

- [ ] All affected layers updated (entity → repository → service → controller → DTOs → mapper)
- [ ] Input validation on Request DTOs
- [ ] Service throws typed exceptions; no `null` returns for "not found"
- [ ] Unit test for `ServiceImpl` covers happy path + error cases
- [ ] Flyway migration added if schema changed (new file, never modify existing)
- [ ] No raw `@Entity` leaked to controller responses
- [ ] `./mvnw clean verify` passes
- [ ] Swagger renders correctly at `/swagger-ui.html`
- [ ] Conventional commit message