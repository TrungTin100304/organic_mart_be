# CLAUDE.md

This file gives Claude Code the context it needs for `organic_mart_be`.
For universal rules that apply to any AI agent, see `AGENTS.md`.

---

## Project at a glance

Spring Boot 3 / Java 21 backend monolith for organic-mart.
Stateless REST API · JWT auth · PostgreSQL + Flyway · JPA/Hibernate · MapStruct.

---

## Always load these into context

@AGENTS.md
@docs/ARCHITECTURE.md
@docs/CONVENTIONS.md
@docs/STACK.md

## Load when relevant

@docs/patterns/api-design.md
@docs/patterns/jpa-patterns.md
@docs/patterns/spring-layer-pattern.md

> Use `@path/to/file.md` syntax to pull file contents into context when working on related code.

---

## Build commands

```bash
./mvnw compile -q                                          # quick compile check
./mvnw test                                                # run all tests
./mvnw clean verify                                        # full build with checks
./mvnw test -Dtest=UserServiceImplTest                     # single test class
./mvnw test -Dtest=UserServiceImplTest#shouldCreateUser    # single test method
./mvnw spring-boot:run -Dspring-boot.run.profiles=local    # run locally
```

After running locally: http://localhost:8080/swagger-ui.html

---

## Hard rules (most important — see AGENTS.md for the full list)

- Never use `@Data` on JPA entities
- Never return raw `@Entity` from controllers — always Response DTO
- Never modify an existing Flyway migration — add a new one
- Never set `FetchType.EAGER` globally; use `JOIN FETCH` or `@EntityGraph` per query
- Never put business logic in controllers
- Service throws typed exceptions for "not found"; never returns `null`
- Add dependencies to `pom.xml` only with explicit user approval

---

## Available skills

Workflow skills are in `.claude/skills/`. Claude Code loads them automatically when relevant:

- **`kn-spec`** — Create a feature specification. Use when starting a new feature from a rough idea.
- **`kn-plan`** — Create an implementation plan from a spec. Use after `spec/{feature}.md` exists.
- **`kn-implement`** — Implement a feature layer-by-layer from spec + plan. Use after `plan/{feature}.md` exists.

Each skill reads `AGENTS.md`, `docs/CONVENTIONS.md`, `docs/ARCHITECTURE.md` at the start.

---

## Working preferences for this project

- **Plan before coding for multi-file changes.** List every file you'll CREATE or MODIFY before writing any code. Wait for user confirmation if the change touches more than 3 files outside a single domain module.
- **Run `./mvnw compile -q` after writing each layer.** Catches MapStruct generation and Lombok issues early.
- **Run `./mvnw test` before marking a task done.** Don't claim "should work" without verification.
- **Use TodoWrite for multi-step tasks** to keep state visible.
- **When unsure about a pattern**, read the matching file in `docs/patterns/` before proposing code.

---

## Repository structure

```
organic_mart_be/
├── src/main/java/com/example/organicmart/
│   ├── domain/{module}/         # JPA entities
│   ├── repository/{module}/     # Spring Data JPA interfaces
│   ├── service/{module}/        # Business logic + @Transactional
│   ├── web/{module}/            # Controllers, DTOs, mappers
│   ├── security/                # JWT filter, SecurityConfig
│   ├── exception/               # Custom exceptions + GlobalExceptionHandler
│   ├── common/                  # ApiResponse, PageResponse, utils
│   └── config/                  # Spring @Configuration
├── src/main/resources/
│   ├── db/migration/            # Flyway V{n}__{name}.sql files
│   └── application*.yml
├── src/test/java/...            # Tests mirror main package layout
├── spec/                        # Feature specifications
├── plan/                        # Implementation plans
├── docs/                        # Source-of-truth documentation
└── pom.xml
```

---

## When something feels unclear

1. Check `docs/patterns/` first — most JPA / API / layer questions are answered there
2. Check `AGENTS.md` for hard constraints
3. Ask the user — don't guess on business rules, schema decisions, or dependency additions