# TaskFlow — Session Context & History

> **What this file is.** A human-readable backup of the work and decisions made with Claude Code on this
> project, so you never lose context if a chat session disappears from the UI.
>
> **How to use it.** Read it to remind yourself where things stand. If you ever start a *fresh* Claude Code
> session and it has no memory of the past, paste (or point Claude at) this file to restore context instantly.
>
> _Last updated: 2026-06-08. Covers everything through Phase 7._

---

## 1. Where Claude Code saves your chats (and how to reopen them)

Every conversation is stored as a `.jsonl` file in:

```
C:\Users\Fluxtech Solutions\.claude\projects\c--Users-Fluxtech-Solutions-taskflow-api\
```

Known sessions so far:

| File | When | What happened in it |
|------|------|---------------------|
| `ff0591df-…jsonl` | Jun 1–3 | Phases 1–7 originally built |
| `35e208c2-…jsonl` | Jun 4–8 | Recovery after laptop-off, package restructure, verification, the PDFs |

**To reopen a past chat**, run from a terminal inside the `taskflow-api` folder:

- `claude --continue` (`-c`) — reopen the **most recent** conversation.
- `claude --resume` (`-r`) — show a **list of all past sessions** and pick one.

These read the `.jsonl` files above, so history is recoverable even when the editor panel doesn't show it.
This Markdown file is the easy-to-read backup; the `.jsonl` files are the full literal logs.

---

## 2. The project at a glance

- **TaskFlow** = a personal-productivity **REST API** (the backend only; a Next.js frontend is out of scope).
- **Stack:** Java 21, Spring Boot 3.5.3, Maven, PostgreSQL 16, Spring Security + JWT, Spring Data JPA/Hibernate,
  Flyway migrations, BCrypt, Springdoc/Swagger, JUnit/Mockito/Testcontainers (tests come in Phase 11).
- **Built incrementally in phases**, stopping after each for review. The full plan lives in `CLAUDE.md`.
- **Learner context:** the developer (Ryan) is new to programming in general — explanations are kept beginner-friendly.

### Phase progress
| Phase | Topic | Status |
|------|-------|--------|
| 1 | Project skeleton (pom, config, docker-compose) | ✅ done |
| 2 | Database schema + Flyway (V1, V2 seed) | ✅ done |
| 3 | JPA entities + repositories + auditing | ✅ done |
| 4 | Security foundation (BCrypt, JWT, filter, SecurityConfig) | ✅ done |
| 5 | Auth endpoints + User feature | ✅ done |
| 6 | Categories CRUD | ✅ done |
| 7 | Tasks CRUD + complete-toggle | ✅ **done & verified** |
| 8 | **Task filtering & pagination (JPA Specifications)** | ⏭️ **next** |
| 9–13 | Error handling, analytics, tests, deployment, wrap-up | ⬜ pending |

---

## 3. Current state (2026-06-08)

- **Git branch:** `main` (Phase 7 was merged via squash PR **#7**, commit `13b4e11`).
- **Working tree:** clean, except the `docs/` folder is **untracked** (the guides below — commit them if you want).
- **To start Phase 8:** `git checkout -b feat/phase-8-filtering`
- **Environment facts to remember:**
  - Start **Docker Desktop** first, then `docker compose up -d postgres`.
  - Postgres runs on host port **5433** (a native Postgres already owns 5432).
  - The app runs on **http://localhost:8080**; endpoints are under **/api/v1**; default profile is **dev**.
  - Built-in API tester: **http://localhost:8080/swagger-ui.html**
  - Pre-seeded demo login (dev only): **demo@taskflow.com** / **Password123!**
  - Health check: **http://localhost:8080/actuator/health** → `{"status":"UP"}`

---

## 4. What happened, session by session

### Session 1 (`ff0591df`, ~Jun 1–3) — building Phases 1–7
Built the skeleton, schema, entities, security, auth/user, categories, and the task feature code. Phase 7's
verification step (running the app + curl) had **not** been done when the laptop was switched off.

### Session 2 (`35e208c2`, Jun 4–8) — this session
1. **Recovered context** from git + files on disk (the chat UI had lost the prior session).
2. **Reverted a `.gitignore` mistake** — `.env.example` is the safe committed template and should not be ignored.
3. **Restructured the whole codebase** from flat feature packages into **feature → layer** sub-packages
   (see §5). 24 files edited (package + imports), 20 files moved. `CLAUDE.md`'s package-layout section was
   updated so all future phases follow the new convention.
4. **Verified everything:** `./mvnw compile` → BUILD SUCCESS; started Docker Postgres + the app; ran the full
   Task CRUD curl sequence (register → create → list → read → update → complete-toggle → toggle-back → delete →
   404). All endpoints behaved correctly — proving the restructure didn't break runtime behaviour.
5. **Generated learning PDFs** (see §7).
6. **Explained the git workflow** (GitHub Flow + squash merge); Ryan pushed, opened PR #7, squash-merged, and
   returned to `main`.
7. **Wrote a testing guide** after Ryan couldn't figure out how to test via Postman.

---

## 5. The codebase structure (current layout)

Each **feature** package is split by **layer**. `config` and `common` are cross-cutting and not sub-foldered.

```
com.taskflow.api
├── TaskflowApplication.java
├── config/        SecurityConfig, CorsConfig, PasswordEncoderConfig
├── auth/    controller/(AuthController)  service/(AuthService, JwtService)  filter/(JwtAuthFilter)  dto/
├── user/    controller/  service/  repository/  entity/(User)  dto/
├── category/ controller/ service/ repository/ entity/(Category) mapper/ dto/
├── task/    controller/ service/ repository/ entity/(Task, Priority, TaskStatus) mapper/ dto/
└── common/  audit/(JpaAuditingConfig)
```

> Because folders are Java packages, classes in the same feature now import each other across packages
> (e.g. `TaskService` imports `com.taskflow.api.task.entity.Task`).

---

## 6. Key decisions & their rationale

- **Restructure naming:** layer folders are `controller / service / repository / entity / mapper / dto`
  (Spring-standard, singular). Enums (`Priority`, `TaskStatus`) live in `entity/` beside `Task`. Applied to all
  four feature packages; `config`/`common` left as-is. _Reason: the supervisor wanted a cleaner, uniform look._
- **Ownership is enforced in the SERVICE layer**, not controllers — the one place every request must pass.
  Asking for someone else's item returns **404, not 403**, so it leaks no information.
- **`completedAt` invariant:** a single `applyStatus` helper keeps `status` and `completedAt` consistent
  (set on DONE, cleared otherwise) no matter how a task reaches that state.
- **Schema is owned only by Flyway** (`ddl-auto: validate`); enums stored as text + DB `CHECK` constraints.
- **Docs are generated** by writing styled HTML, then converting with **Chrome headless**
  (`--headless=new --print-to-pdf`). Gotcha: the repo path has a space, which breaks the command — render to a
  space-free temp path (`$env:TEMP`) and move the PDF into `docs/`.

---

## 7. Generated docs & memory

In `docs/` (currently untracked in git):
- `TaskFlow-Codebase-Walkthrough-Phases-1-7.{html,pdf}` — beginner walkthrough of every file with code snippets.
- `TaskFlow-Testing-Guide.{html,pdf}` — how to run & test the API (Swagger, Postman, curl, full endpoint reference).
- `SESSION-CONTEXT-and-History.md` — this file.

Claude's long-term memory for this project lives in
`C:\Users\Fluxtech Solutions\.claude\projects\c--Users-Fluxtech-Solutions-taskflow-api\memory\`
(dev environment, git workflow, beginner-explanation preference, PDF-generation how-to).

---

## 8. The API surface today (all under `/api/v1`)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/auth/register` | 🔓 | create account → token + user |
| POST | `/auth/login` | 🔓 | log in → token + user |
| GET | `/users/me` | 🔒 | your profile |
| GET/POST | `/categories` | 🔒 | list / create |
| GET/PUT/DELETE | `/categories/{id}` | 🔒 | read / update / delete |
| GET/POST | `/tasks` | 🔒 | list / create |
| GET/PUT/DELETE | `/tasks/{id}` | 🔒 | read / update / delete |
| PATCH | `/tasks/{id}/complete` | 🔒 | toggle done-state |

🔒 = needs `Authorization: Bearer <token>` header. (Phase 8 will add filter/pagination query params to `GET /tasks`.)

---

## 9. What's next — Phase 8

**Task filtering & pagination using JPA Specifications.** Planned additions to `GET /tasks`:
- Query params: `page`, `size`, `sort`, plus filters `status`, `priority`, `categoryId`, `dueBefore`, `search`.
- A new `task/spec/` package holding the Specification building blocks (dynamic WHERE clauses).
- A `PageResponse<T>` wrapper for paginated output.
- `CLAUDE.md` flags this as the phase deserving the most teaching (Specifications are the most advanced concept).

To begin: `git checkout -b feat/phase-8-filtering`, then tell Claude **"continue"** / **"start Phase 8."**

---

## 10. Restoring context in a new Claude session

1. Open the `taskflow-api` folder in VS Code (or `cd` into it in a terminal).
2. Try `claude --resume` to reopen a past session, or start fresh.
3. If starting fresh, say: _"Read CLAUDE.md and docs/SESSION-CONTEXT-and-History.md to catch up, then let's continue from Phase 8."_
4. Claude also auto-loads `CLAUDE.md` and its project memory, so most context returns automatically.
