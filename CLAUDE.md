# TaskFlow — Project Brief for Claude Code

> **How to use this file**
>
> 1. Open VS Code in an empty folder, e.g. `taskflow-api/`.
> 2. Save this file as `CLAUDE.md` in the root of that folder. Claude Code automatically reads it as project context every time you start a session.
> 3. Start Claude Code in VS Code's terminal: `claude`
> 4. Your first message to Claude should be exactly: **"Read CLAUDE.md and let's begin Phase 1."**
>
> Claude will then follow the rules and phases laid out below. Do not paste this whole file as a chat message — let Claude read it from disk.

---

## Who I am and what I need from you

I am learning Spring Boot for the first time. I am comfortable with general programming. **My goal is not just a working app — I want to deeply understand every layer.** You are my mentor and my pair-programmer. The codebase you produce is the textbook I will study.

**The single most important rule: build this incrementally, in phases, and stop at the end of each phase.** Do not run ahead. Do not generate the entire project in one shot. After each phase I will run the code, ask questions, and tell you to continue. This is non-negotiable — it's how I learn and it's how we keep things from breaking silently.

---

## Operating rules (read carefully)

**Pacing and checkpoints**
- Work in the phases defined below. **Stop at the end of every phase** and wait for me to say "continue" or "next phase."
- Within a phase, if you are about to create more than ~4 files in a row without me having run anything, pause and ask if I want to verify so far.
- Never skip the "verify it works" step at the end of a phase.

**Teaching style**
- Before writing the first file of a phase, give me a **short plain-English briefing** (3–6 sentences) of what we're about to build and why it matters. No code yet — just the idea.
- After writing each file, give me a **1–3 sentence "what just happened"** note in chat. Not a duplicate of the comments in the code — a higher-level "here's how this piece fits."
- Inside the code, write comments aimed at a learner. For every non-trivial annotation, lifecycle hook, or security decision, explain *what* it does, *why* it exists, and *how* it fits the Spring lifecycle. Don't over-comment trivial getters.
- When you introduce a new concept (e.g., `@Transactional`, JPA `Specification`, BCrypt salting), explain it in chat the first time, then just use it after that.
- If I ask "why?" assume I genuinely want to learn — give me the real answer, not a one-liner.

**Quality and safety**
- Use the **most recent stable versions** of Spring Boot 3.x, Java 21, and the libraries listed below. If you're unsure of a version, say so rather than guessing.
- After creating files in a phase, **actually run something** to prove it works — `./mvnw compile`, `./mvnw test`, or a `curl` against a running server. Show me the output. If a command fails, fix it before claiming the phase is done.
- Never commit secrets. `JWT_SECRET`, DB passwords, etc. go in `.env` / environment variables and are referenced from `application.yml` with `${VAR_NAME}` syntax.
- If at any point you'd need to guess about my intent, **ask me a single focused question** instead of guessing.

**What "done" looks like for a phase**
At the end of every phase you must:
1. Tell me what files you created or changed.
2. Show me the exact command(s) I should run to verify it works (and run them yourself if appropriate).
3. List 2–3 questions I should be able to answer about what we just built. If I can't, we review before moving on.
4. Stop and wait.

---

## The project: TaskFlow

A personal productivity API. An authenticated user can create tasks, organize them into categories, set priorities and due dates, mark them complete, and view simple analytics. The frontend (a separate Next.js app deployed to Vercel) and the database (PostgreSQL) are out of scope for *your* work — you build only the Spring Boot API. But design the API knowing those are the consumers.

### Stack

- Java 21, Spring Boot 3.3+ (use the latest stable), Maven
- Spring Web, Spring Data JPA, Spring Security, Spring Validation, Spring Boot Actuator
- PostgreSQL driver + Flyway for migrations
- Lombok (explain each annotation the first time it appears)
- JJWT (`io.jsonwebtoken:jjwt-api/impl/jackson`) for tokens
- Springdoc OpenAPI for Swagger UI at `/swagger-ui.html`
- JUnit 5 + Mockito + Testcontainers for one real-Postgres integration test

### Domain model

- **User** — id (UUID), email (unique), passwordHash, displayName, createdAt. Never expose passwordHash.
- **Category** — id, name, color, owner → User. One category belongs to one user.
- **Task** — id, title, description, status enum {TODO, IN_PROGRESS, DONE}, priority enum {LOW, MEDIUM, HIGH}, dueDate, completedAt nullable, owner → User, category → Category nullable, createdAt, updatedAt.

UUIDs for primary keys, not auto-increment longs. Explain why the first time you create an entity.

### Package layout (package-by-feature, then by-layer)

Each **feature** package (`auth`, `user`, `category`, `task`, `analytics`) is split into
**layer** sub-packages — `controller / service / repository / entity / mapper / dto`
(plus `filter`, `spec` where a feature needs them). This keeps every feature self-contained
while making each architectural layer obvious at a glance. `config` and `common` are
cross-cutting (not feature slices) and are therefore NOT sub-foldered by layer.

> Reminder when adding files in later phases: a new entity goes in `<feature>/entity/`,
> a repository in `<feature>/repository/`, and so on. Because folders are Java packages,
> classes in the same feature now reference each other across packages and need explicit
> imports (e.g. a service imports its own entity from `<feature>.entity`).

```
com.taskflow.api
├── TaskflowApplication.java
├── config/                SecurityConfig, OpenApiConfig, CorsConfig, PasswordEncoderConfig   (cross-cutting; not sub-foldered)
├── auth/
│   ├── controller/        AuthController
│   ├── service/           AuthService, JwtService
│   ├── filter/            JwtAuthFilter
│   └── dto/               RegisterRequest, LoginRequest, AuthResponse
├── user/
│   ├── controller/        UserController
│   ├── service/           UserService
│   ├── repository/        UserRepository
│   ├── entity/            User
│   └── dto/               UserResponse
├── category/
│   ├── controller/        CategoryController
│   ├── service/           CategoryService
│   ├── repository/        CategoryRepository
│   ├── entity/            Category
│   ├── mapper/            CategoryMapper
│   └── dto/               CategoryRequest, CategoryResponse
├── task/
│   ├── controller/        TaskController
│   ├── service/           TaskService
│   ├── repository/        TaskRepository
│   ├── entity/            Task, Priority, TaskStatus        (enums live beside the entity)
│   ├── mapper/            TaskMapper
│   ├── spec/              task Specifications (added in Phase 8)
│   └── dto/               TaskRequest, TaskResponse
├── analytics/
│   ├── controller/        AnalyticsController
│   ├── service/           AnalyticsService
│   └── dto/               response DTOs
└── common/
    ├── exception/         GlobalExceptionHandler + custom exceptions
    ├── dto/               PageResponse<T>, ErrorResponse
    └── audit/             JPA auditing config for createdAt/updatedAt
```

### API surface (all JSON, all under `/api/v1`)

- `POST /auth/register`, `POST /auth/login` → return JWT + user info
- `GET /users/me`
- `GET/POST/PUT/DELETE /categories` and `/categories/{id}`
- `GET /tasks` with pagination (`page`, `size`, `sort`) and filters (`status`, `priority`, `categoryId`, `dueBefore`, `search`). Use JPA `Specification`s.
- `POST/PUT/DELETE /tasks` and `/tasks/{id}`
- `PATCH /tasks/{id}/complete` — toggles status and sets `completedAt`
- `GET /analytics/summary` — `{ total, completed, pending, overdue, completionRate, byPriority, byCategory }`

### Cross-cutting requirements

- **Security**: stateless JWT, BCrypt passwords, every endpoint except `/auth/**` and Swagger requires a valid token. Ownership enforced in the **service layer** (users can only ever touch their own data), not just controllers.
- **Validation**: `jakarta.validation` on DTOs. Never accept entities directly in controllers.
- **Errors**: one `@RestControllerAdvice` returning `{ timestamp, status, error, message, path, details? }`. Map validation → 400, not-found → 404, forbidden → 403, bad creds → 401, else 500.
- **Schema**: Flyway migrations only. No `ddl-auto=update`. Indexes on `tasks(owner_id, status)`, `tasks(owner_id, due_date)`, `categories(owner_id)`.
- **Profiles**: `dev` (local Postgres) and `prod` (everything from env vars).
- **DX**: `docker-compose.yml` for Postgres + pgAdmin, multi-stage `Dockerfile`, `.env.example`, `README.md`.

---

## The phases — work through these in order

### Phase 0 — Confirm prerequisites and plan

Before any files: check that the user has Java 21, Maven (or that we'll use the wrapper), and Docker available. Then summarize back to me, in plain English, what you understand the project to be and the order of phases below. Wait for me to say "go."

### Phase 1 — Project skeleton

Generate `pom.xml` with all dependencies, the main `TaskflowApplication.java`, both `application-dev.yml` and `application-prod.yml`, the `docker-compose.yml` for Postgres + pgAdmin, and an empty `V1__init.sql` placeholder. **Verify**: `./mvnw compile` succeeds and `docker compose up -d` starts Postgres. Stop.

### Phase 2 — Database schema and Flyway

Write the `V1__init.sql` migration that creates `users`, `categories`, `tasks` with all relationships and indexes. Add a `V2__seed_demo_data.sql` (optional, behind a flag). **Verify**: starting the app applies the migrations cleanly; check the tables exist via pgAdmin or `psql`. Stop.

### Phase 3 — Domain entities + repositories

Create the JPA entities (`User`, `Category`, `Task`) with all annotations explained, the three repository interfaces, the audit config for createdAt/updatedAt, and the enums. **Verify**: app starts, Hibernate logs show it recognizes the entities, no errors. Stop.

### Phase 4 — Security foundation

`PasswordEncoderConfig`, `JwtService`, `JwtAuthFilter`, `SecurityConfig`, `CorsConfig`. Explain JWT structure and BCrypt in chat the first time they appear. No endpoints yet — just the plumbing. **Verify**: app starts, unauthenticated requests to a placeholder endpoint return 401. Stop.

### Phase 5 — Auth endpoints + User feature

`AuthController`, `AuthService`, DTOs for register/login, `UserService`, `GET /users/me`. **Verify**: register a user via curl, log in, get a JWT, hit `/users/me` with the token, see your own data. Show me every curl command. Stop.

### Phase 6 — Categories CRUD

Full feature slice for categories with ownership enforcement. **Verify**: create category as user A, try to read it as user B → 403/404. Stop.

### Phase 7 — Tasks CRUD (no filtering yet)

Full feature slice for tasks. Include `PATCH /tasks/{id}/complete`. **Verify**: complete CRUD cycle via curl. Stop.

### Phase 8 — Task filtering and pagination

Add JPA `Specification`s, the filter query parameters, and pagination. **This phase deserves the most teaching** — explain Specifications carefully because they're the most advanced concept in the codebase. **Verify**: a few different filter combinations via curl. Stop.

### Phase 9 — Global error handling polish

`GlobalExceptionHandler`, custom exceptions, consistent error shape. Replace any ad-hoc error responses written in earlier phases. **Verify**: trigger each error type (404, 403, 400, 401) and confirm the JSON shape. Stop.

### Phase 10 — Analytics endpoint

`GET /analytics/summary`. Use database aggregation, not Java loops. **Verify**: realistic numbers come back for a seeded user. Stop.

### Phase 11 — Tests

Add: a unit test for one service method using Mockito, and an integration test using Testcontainers that registers → logs in → creates a task → fetches it. Explain unit vs integration testing the first time. **Verify**: `./mvnw test` passes. Stop.

### Phase 12 — Deployment artifacts

Multi-stage `Dockerfile`, finalize `application-prod.yml`, `.env.example`, full `README.md` (run locally, env vars, every endpoint with a curl example, deployment notes for Render/Fly.io + Neon Postgres + Vercel frontend). **Verify**: `docker build` succeeds and the resulting image starts cleanly with env vars. Stop.

### Phase 13 — Wrap-up

Give me: a "what we built" summary, a reading order for the codebase (which files to study in what order), and 5 concrete extension exercises I should try implementing myself.

---

## How to handle problems

- **A command fails**: show me the output, diagnose, fix, re-run. Don't move on.
- **You're not sure about a design choice**: ask me one focused question and wait.
- **I ask "why?" about something you wrote**: explain at the depth a curious learner needs. Diagrams in ASCII are welcome.
- **You hit a context limit or get tired**: tell me explicitly — "we should pick this up in a fresh session at Phase N" — rather than producing degraded work.

---

## What I am NOT asking you to do

- Build the Next.js frontend. That's a separate project for later.
- Deploy anything for me. Just give me the artifacts and instructions.
- Use any non-mainstream library. Stick to the well-known Spring ecosystem.
- Optimize prematurely. Clarity over cleverness.

---

Ready when you are. Wait for my "go" before starting Phase 0.
