# TaskFlow — Session Context & History

> **What this file is.** A human-readable backup of the work and decisions made with Claude Code on this
> project, so you never lose context if a chat session disappears from the UI.
>
> **How to use it.** Read it to remind yourself where things stand. If you ever start a *fresh* Claude Code
> session, point Claude at this file to restore context instantly.
>
> _Last updated: 2026-06-11. Covers everything through Phase 10 + the Next.js frontend._

---

## 1. Where Claude Code saves your chats (and how to reopen them)

Every conversation is stored as a `.jsonl` file in:

```
C:\Users\Fluxtech Solutions\.claude\projects\c--Users-Fluxtech-Solutions-taskflow-api\
```

**To reopen a past chat**, run from a terminal inside the `taskflow-api` folder:

- `claude --continue` (`-c`) — reopen the **most recent** conversation.
- `claude --resume` (`-r`) — show a **list of all past sessions** and pick one.

> The standalone `claude` CLI must be installed for the commands above:
> `npm install -g @anthropic-ai/claude-code` (then open a NEW terminal). The VS Code extension is a separate
> install and also keeps its own session history.

---

## 2. The project at a glance

- **TaskFlow** = a personal-productivity app: a **Spring Boot REST API** (`/`) + a **Next.js frontend** (`/frontend`).
- **Backend stack:** Java 21, Spring Boot 3.5.3, Maven, PostgreSQL 16, Spring Security + JWT, Spring Data
  JPA/Hibernate, Flyway migrations, BCrypt, Springdoc/Swagger.
- **Frontend stack:** Next.js 16 (App Router) + React 19 + TypeScript + Tailwind CSS v4.
- **Built incrementally in phases.** The full plan lives in `CLAUDE.md`.
- **Learner context:** Ryan is new to programming in general — keep explanations beginner-friendly, with analogies
  and real code snippets.

### Phase progress
| Phase | Topic | Status |
|------|-------|--------|
| 1 | Project skeleton (pom, config, docker-compose) | ✅ done |
| 2 | Database schema + Flyway (V1, V2 seed) | ✅ done |
| 3 | JPA entities + repositories + auditing | ✅ done |
| 4 | Security foundation (BCrypt, JWT, filter, SecurityConfig) | ✅ done |
| 5 | Auth endpoints + User feature | ✅ done |
| 6 | Categories CRUD | ✅ done |
| 7 | Tasks CRUD + complete-toggle | ✅ done |
| 8 | Task filtering & pagination (JPA Specifications) | ✅ done (PR #8) |
| 9 | Global error handling (consistent error shape) | ✅ done (PR #9) |
| 10 | Analytics summary endpoint (DB aggregation) | ✅ done (PR #10) |
| — | **Next.js + Tailwind frontend** | ✅ done (PR #11) |
| 11 | Tests (Mockito unit + Testcontainers integration) | ⬜ pending |
| 12 | Deployment artifacts (Dockerfile, prod config, README) | ⬜ **next planned** |
| 13 | Wrap-up (reading order + extension exercises) | ⬜ pending |

> Ryan reprioritized: do the most important backend phases (9, 10) then the frontend **before** deploying.
> Remaining order: **Phase 12 (deploy) → Phase 11 (tests) → Phase 13 (wrap-up)**.

---

## 3. Current state (2026-06-11)

- **Git branch:** `main`, latest commit `e5a98f0` (frontend, PR #11). Backend phases merged as PRs #8/#9/#10.
- **Claude now runs git commands** for Ryan this session (branch → commit → PR → squash-merge) to fast-track.
- **Environment / how to run:**
  - Start **Docker Desktop**, then `docker compose up -d postgres` (Postgres on host port **5433**).
  - Backend: `./mvnw spring-boot:run` → **http://localhost:8080** (endpoints under `/api/v1`, dev profile).
  - Frontend: `cd frontend && npm run dev` → **http://localhost:3000**.
  - Swagger: **http://localhost:8080/swagger-ui.html** · Demo login: **demo@taskflow.com / Password123!**

---

## 4. How the frontend connects to the backend (the link)

- The frontend (browser, `localhost:3000`) calls the API (`localhost:8080/api/v1`) with `fetch`.
- **CORS:** `config/CorsConfig.java` allows the origin `http://localhost:3000`, so the browser permits the calls.
- **Auth:** login returns a **JWT**; the frontend stores it in `localStorage` and attaches it as
  `Authorization: Bearer <token>` on every request (see `frontend/src/lib/api.ts`).
- **The bridge file:** `frontend/src/lib/api.ts` — one typed `request()` wrapper + functions like `apiTasks`,
  `apiCreateTask`, `apiAnalytics`. `frontend/src/lib/auth.tsx` holds the logged-in user in React context.
- Full detail + a traced example is in `docs/TaskFlow-FullStack-and-Spotting-Spring.pdf`.

---

## 5. The codebase structure (current)

```
taskflow-api/
├── src/main/java/com/taskflow/api/
│   ├── TaskflowApplication.java
│   ├── config/        SecurityConfig, CorsConfig, PasswordEncoderConfig
│   ├── auth/          controller/ service/(AuthService, JwtService) filter/(JwtAuthFilter) dto/
│   ├── user/          controller/ service/ repository/ entity/(User) dto/
│   ├── category/      controller/ service/ repository/ entity/(Category) mapper/ dto/
│   ├── task/          controller/ service/ repository/ entity/(Task, Priority, TaskStatus) mapper/ spec/ dto/
│   ├── analytics/     controller/ service/ dto/(AnalyticsSummary)
│   └── common/        audit/(JpaAuditingConfig) dto/(PageResponse, ErrorResponse) exception/(GlobalExceptionHandler + ApiException family)
├── src/main/resources/  application*.yml, db/migration/V1, db/seed/V2
└── frontend/          Next.js app — src/app/(login, register, tasks, categories, analytics) + src/lib/(api, auth, types) + src/components/
```

---

## 6. Key decisions & rationale (cumulative)

- **Package-by-feature, then by-layer** (`controller/service/repository/entity/mapper/dto`). Supervisor's request.
- **Ownership enforced in the SERVICE layer**; "not yours" returns **404, not 403** (no info leak).
- **`completedAt` invariant** kept by one `applyStatus` helper.
- **Schema owned only by Flyway** (`ddl-auto: validate`); enums stored as text + DB `CHECK`.
- **Phase 8 filtering** uses `Specification.allOf(...)` (not deprecated `where`) to compose dynamic filters.
- **Phase 9 errors:** one `@RestControllerAdvice` + an `ApiException` family → consistent
  `{ timestamp, status, error, message, path, details? }`.
- **Phase 10 analytics:** all counts via DB `COUNT`/`GROUP BY` in `TaskRepository` (no Java loops over tasks).
- **Frontend** is client-rendered (SPA-style); JWT in `localStorage`; built on Next 16 (read the bundled docs in
  `frontend/node_modules/next/dist/docs` — it's a new major with breaking changes).
- **Docs** generated as HTML → Chrome headless `--print-to-pdf`. Gotcha: repo path has a space → render to a
  space-free temp path (`$env:TEMP`) then move into `docs/`.

---

## 7. Generated docs (in `docs/`)

- `TaskFlow-Codebase-Walkthrough-Phases-1-7.{html,pdf}` — beginner walkthrough of every backend file (Phases 1–7).
- `TaskFlow-Testing-Guide.{html,pdf}` — run & test the API (Swagger, Postman, curl, endpoint reference).
- `TaskFlow-FullStack-and-Spotting-Spring.{html,pdf}` — how the frontend links to the backend + a guide to
  **recognizing Spring building blocks (DTO, repository, service, etc.) in the actual code**.
- `SESSION-CONTEXT-and-History.md` — this file.

Claude's long-term memory is in
`C:\Users\Fluxtech Solutions\.claude\projects\c--Users-Fluxtech-Solutions-taskflow-api\memory\`.

---

## 8. The API surface today (all under `/api/v1`)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/auth/register` | 🔓 | create account → token + user |
| POST | `/auth/login` | 🔓 | log in → token + user |
| GET | `/users/me` | 🔒 | your profile |
| GET/POST | `/categories` · `/categories/{id}` (GET/PUT/DELETE) | 🔒 | category CRUD |
| GET | `/tasks` | 🔒 | list with filters (`status, priority, categoryId, dueBefore, search`) + pagination (`page, size, sort`) |
| POST/PUT/DELETE | `/tasks` · `/tasks/{id}` | 🔒 | task CRUD |
| PATCH | `/tasks/{id}/complete` | 🔒 | toggle done-state |
| GET | `/analytics/summary` | 🔒 | totals, completionRate, byPriority, byCategory |

🔒 = needs `Authorization: Bearer <token>`. Errors return the consistent shape (Phase 9).

---

## 9. What's next — Phase 12 (Deployment)

Multi-stage `Dockerfile` for the API, finalize `application-prod.yml`, `.env.example`, full `README.md` with
run/deploy notes (Render/Fly.io + Neon Postgres for the API; Vercel for the frontend). Then Phase 11 (tests) and
Phase 13 (wrap-up).

To begin: tell Claude **"start Phase 12"** (Claude will create the branch and handle git).

---

## 10. Restoring context in a new Claude session

1. Open the `taskflow-api` folder in VS Code (or `cd` into it).
2. `claude --resume` to reopen a past session, or start fresh.
3. If fresh, say: _"Read CLAUDE.md and docs/SESSION-CONTEXT-and-History.md to catch up, then let's continue from Phase 12."_
