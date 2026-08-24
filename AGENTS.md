# VetHub — Agent Instructions

VetHub is a full-stack veterinary clinic management application (owners, pets, vets, visits) used as the codebase for the ilionx SkillLab "AI-driven Development" workshop. This file is project memory: read it before touching code, and update it when you discover a new convention or gotcha — it is not write-once.

## Tech stack

- **Backend**: Spring Boot 4.1, Java 25, Gradle, H2 (in-memory), Liquibase, MapStruct, springdoc-openapi, Spring Security
- **Frontend**: SvelteKit 2, Svelte 5, TypeScript, Tailwind CSS, bun, openapi-fetch
- **Toolchain**: versions pinned in `mise.toml` (java, node, bun, opencode) — always `mise exec --` or activate the shell rather than relying on system-installed versions

## Architecture

Two components talking REST/JSON:

- `server/` — Spring Boot API on `:8080`, context path `/api`. CORS allows any `localhost:*` origin with credentials; requests carry `Authorization: Basic ...` (default `user`/`password`).
- `client/` — SvelteKit app on `:5173`, calls the backend through a generated, type-safe `openapi-fetch` client.
- The bridge between them is the OpenAPI spec itself: `scripts/openapi-sync.sh` (`bun run sync:api` from `client/`) boots the backend, downloads its live spec, and regenerates `client/src/lib/types/api.d.ts`. Never hand-edit that file — it's marked auto-generated and will be overwritten.
- `scripts/common.sh`'s `HEALTH_URL` must be `http://localhost:8080/api/v1/public/actuator/health` — the actuator's `base-path` is `/v1/public/actuator` (see `application.yml`), not `/actuator`. If `sync:api` times out with "Backend failed to start", check this first before assuming a real startup failure.

## Package structure — feature-first

Backend code under `server/src/main/java/dev/ilionx/workshop/api/` is organized by **domain**, not by technical layer. `owner/`, `pet/`, `vet/`, `visit/` each repeat the same internal shape:

```
owner/
  controller/   OwnerController.java
  service/      OwnerService.java
  repository/   OwnerRepository.java
  model/
    Owner.java                    -- JPA entity
    request/                      -- CreateOwnerRequest, UpdateOwnerRequest
    response/                     -- OwnerResponse, PetSummaryResponse
    mapper/                       -- OwnerMapper (MapStruct)
    validator/                    -- OwnerValidator
```

`pet/` and `visit/` are nested under `owner`/`pet` in the URL space, so each has **two** controllers: a nested one (`PetController` → `/api/owners/{ownerId}/pets...`) and a global one (`PetGlobalController` → `/api/pets...`), both backed by the same `PetService`. `owner`/`vet` have only one controller each. Route path constants are centralized in `api/Paths.java` — add new routes there, don't inline path strings in controllers.

## Key patterns & conventions

- **Theming**: `client/src/app.css` defines the Light palette on `:root` and two override blocks, `[data-theme='dark']` and `[data-theme='fancy']` — each restates only the tokens that differ, never duplicates the whole palette. Components must consume tokens (`bg-card`, `text-foreground`), never branch on theme directly. The active theme is set as `data-theme` on `<html>` by an inline bootstrap script in `app.html` (reads `localStorage['vethub:theme']`, falls back to `prefers-color-scheme`) — this runs before hydration to avoid a flash of the wrong theme. `@custom-variant dark (&:where([data-theme='dark'], [data-theme='dark'] *))` in `app.css` is required — without it, Tailwind v4's `dark:` utilities default to `prefers-color-scheme` and ignore the selected theme entirely. When adding a new themeable value (a color, shadow, gradient, etc.), add the token to `:root` first, then override it only in the theme blocks where it actually differs — check whether it needs a value in *every* block, since a token defined in `fancy` but forgotten in `dark` silently falls back to the `:root` (Light) value.
- **DTOs**: separate `CreateXRequest` / `UpdateXRequest` / `XResponse` classes per domain — entities are never returned directly from controllers.
- **Mapping is asymmetric**: MapStruct (`@Mapper(config = SharedMapperConfig.class)`, config supplied by the internal `jframe` library, not this repo) only maps entity → response. Request → entity is done by hand, field-by-field, inside the `Service`'s `create`/`update` methods. Don't try to add a MapStruct method for request→entity — that's not the pattern here.
- **Validation**: custom `Validator` components (e.g. `OwnerValidator`), not Jakarta Bean Validation annotations on the entity or DTOs. Controllers call `xValidator.validateAndThrow(request)` explicitly as the first line of every mutating endpoint.
- **Error handling**: no local `@ControllerAdvice` in this repo — it's `JFrameResponseEntityExceptionHandler` in the external `jframe` `starter-core` library. Throw `DataNotFoundException`, `ValidationException`, etc. with an `ApiErrorCode` value (`common/exception/ApiErrorCode.java`); the enum itself carries no HTTP status — status is determined by which exception subclass you throw (`DataNotFoundException` → 404, etc.).
- **Database migrations**: Liquibase changesets live in `server/src/main/resources/db/changelog/changesets/`, named `<timestamp>-<CONTEXT>-<description>.xml`. Changesets with no `context` attribute always run (schema); changesets tagged `context="tst"` only run when `spring.liquibase.contexts` includes `tst` (true in the `dev` profile via `application-dev.yml`, false in the base/`prd` config) — this is how seed data appears locally but not in a "production" profile. **Never edit an existing changeset** — always add a new one, Liquibase tracks what's already applied.
- **Testing**: two abstract base classes under `server/src/test/java/dev/ilionx/workshop/support/`. `UnitTest` — Mockito only, no Spring context, for services/validators. `IntegrationTest` — full `@SpringBootTest` + MockMvc + real H2/Liquibase, for controllers. Use the lightest one that fits; don't reach for `IntegrationTest` to test pure business logic.
- **Code style**: Spotless auto-formats on every `JavaCompile` (`JFrameStyle` Eclipse profile under `server/src/quality/config/spotless/`). Don't hand-format — just build and let it run.
- **Frontend API access**: one `openapi-fetch` client instance (`client/src/lib/api/client.ts`) → thin per-domain wrapper functions (`client/src/lib/api/owner/OwnerController.ts`) → Svelte components call those wrappers directly inside `$effect`. There are **no SvelteKit `load` functions** in this app — data fetching is client-side only, not SSR.
- **Package manager**: use `bun`, not `npm`, in `client/` — the repo ships `bun.lock` and `mise.toml` pins `bun`. `npm install` will work but drifts from the lockfile.
- **Security posture**: CORS + HTTP Basic auth are configured (`WebSecurityConfig`), but `anyRequest().permitAll()` means nothing is actually enforced yet. This is a deliberate starting state for the workshop — don't "fix" it unless a task explicitly asks for it.

## Common task: adding a field to an entity

Example — adding `email` to `Owner`, in order:

1. `Owner.java` — add the field/column mapping.
2. New Liquibase changeset in `db/changelog/changesets/` (don't touch existing ones) — `ALTER TABLE owners ADD COLUMN email VARCHAR(255)`.
3. `CreateOwnerRequest` / `UpdateOwnerRequest` — add the field with `@Schema` metadata.
4. `OwnerResponse` — add the field.
5. `OwnerValidator` — add a rule if the field needs validation (format, length, required).
6. `OwnerMapper` — usually nothing to do; MapStruct maps matching field names automatically. Only add an explicit `@Mapping` if the name or shape differs.
7. `OwnerService.create`/`update` — add the manual field copy (mapping is asymmetric, see above).
8. `client/`: run `bun run sync:api` to regenerate `api.d.ts`, then update whatever form/display components need the new field.

## Story workflow

Feature work is tracked as files, not tickets:

- **Refining** a vague request: use the `refinement-agent` subagent (`.claude/agents/refinement-agent.md`). It researches the codebase, drafts a story with an ASCII wireframe and explicit decision points, and — once its draft is confirmed — writes it to `.claude/refined/<kebab-case-title>.md`.
- **Building** a refined story: pick it up from `.claude/refined/`, implement it, verify it (tests, type checks, and a manual pass if it's UI-facing).
- **Completing** a story: once a build from `.claude/refined/` is verified working, move that story's file to `.claude/completed/<same-filename>.md` (`git mv` if already committed, plain `mv` otherwise) and append a row to `.claude/CHANGELOG.md`: `| YYYY-MM-DD | <feature name> | <one-line summary> |`. Do this directly — it's simple file housekeeping, not something that needs its own agent invocation.

## Commands

| Task | Command |
|---|---|
| Start backend | `cd server && ./gradlew bootRun` |
| Start frontend | `cd client && bun run dev` |
| Run backend tests | `cd server && ./gradlew test` |
| Format code | `cd server && ./gradlew spotlessApply` |
| Regenerate API types | `cd client && bun run sync:api` |
| Check frontend types | `cd client && bun run check` |
