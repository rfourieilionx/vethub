---
epic: "VISIT-HISTORY"
title: "Visit History & Attending Vet"
estimate: L
status: ready
created: 2026-08-24
depends_on: []
labels: [backend, frontend]
priority: P1
---

## 1. User Story
**As a** clinic staff member (front desk answering an owner's call, or a vet with a pet in the room)
**I want** a complete visit history for any pet that records which vet attended each visit
**So that** I can answer "what was done to my pet, and when?" without pulling paper notes, and see
who treated this animal last before I treat it again

## 2. Business Context & Value
Two problems, one root cause.

*The phone calls*: owners ring the clinic to ask what was done and when. Staff can already reach a
pet's visit list (`/pets/[id]`, `/owners/[id]/pets/[petId]`), but there is no owner-level view, so
an owner with three pets means three separate lookups, and the global `/visits` page shows
`Pet #7` instead of a name (`client/src/routes/visits/+page.svelte:100`) with a permanently
`disabled` "coming soon" action (line 103).

*The clinical half*: "vets could also see what happened before" **cannot be built on the current
schema**. `Visit` has no link to `Vet` — `Visit.java` holds only `date`, `description` and a
`@ManyToOne Pet`. The `visits` table has one FK, to `pets`. Nothing records who attended.
Continuity of care ("Dr. Leary did the spay, ask her") is not just missing from the UI, it was
never captured. This story closes that gap first, then builds the read surfaces on top.

A third, smaller thing gets fixed on the way: `visit/` is the only domain with **no `Validator`**.
`description` is nullable with nothing enforcing it, while `VisitResponse.description` is declared
`requiredMode = REQUIRED` — so a single null-description visit crashes the `/visits` search filter
at `client/src/routes/visits/+page.svelte:17`. A visit history whose entries can be blank does not
solve the problem this story exists to solve.

Note on epic/priority/estimate: `VISIT-HISTORY` / P1 / L are placeholders — no project board was
supplied at refinement time. Replace before sprint planning.

**Refinement decisions (confirmed by the requester, 2026-08-24):** staff-facing only, no owner
portal or auth; a single nullable `@ManyToOne` vet per visit rather than a join table; `vetId`
required on create and optional on update; the missing `VisitValidator` is in scope; the
`GET /v1/owners/{ownerId}/visits` roll-up is in scope; `ON DELETE SET NULL` when a vet is deleted;
seed data back-filled via a new `TST` changeset; kept as one story rather than split
backend/frontend. The reasoning behind each is carried in the relevant section below.

## 3. Acceptance Criteria

- [ ] **AC1: A visit records the attending vet**
  - Given I record a new visit for a pet
  - When I submit the form
  - Then the visit is persisted with the vet I selected, and `GET /v1/visits/{id}` returns
    `vetId`, `vetFirstName` and `vetLastName`

- [ ] **AC2: Existing visit records survive the change**
  - Given visits already exist in the database with no vet
  - When the new Liquibase changeset runs
  - Then `vet_id` is added as a **nullable** column, no existing row is lost or rejected, and those
    visits render as "Seen by — not recorded" rather than erroring

- [ ] **AC3: The attending vet is chosen when recording a visit**
  - Given I am on `/owners/{ownerId}/pets/{petId}/visits/new`
  - When the page loads
  - Then a "Seen by" dropdown is populated from `GET /v1/vets`, showing `Dr. {firstName} {lastName}`
  - And the Record Visit button stays disabled until a vet is selected
  - Given I submit without a vet (e.g. via the API directly)
  - Then the API responds 400 with a validation error naming the `vetId` field

- [ ] **AC4: Pet visit history shows attribution and is newest-first**
  - Given a pet with several visits
  - When I open `/pets/{id}` or `/owners/{ownerId}/pets/{petId}`
  - Then each visit card shows description, formatted date, and "Seen by Dr. {first} {last}"
    (or "— not recorded"), ordered most recent first
  - And the ordering comes from the server, not from an in-template `.sort()` mutation

- [ ] **AC5: The global visits list is actually usable**
  - Given visits exist
  - When I open `/visits`
  - Then each row shows Date, Description, Pet name, Owner name and Seen by — not `Pet #7`
  - And the row action navigates to `/pets/{petId}` instead of being `disabled`
  - And search matches description, pet name, owner name and vet name, case-insensitively
  - And a "All vets ▾" dropdown filters by attending vet, combining with the search term
  - And the footer still reads "Showing N of M visits"

- [ ] **AC6: Owner-level visit history (the phone-call surface)**
  - Given owner 6 has two pets with four visits between them
  - When I open `/owners/6`
  - Then a "Visit History" section below Pets lists all four visits across both pets, newest first,
    each labelled with which pet it was for and which vet attended
  - And it is served by a single request to `GET /v1/owners/{ownerId}/visits`, not one request per pet

- [ ] **AC7: Visits are validated**
  - Given a `POST`/`PUT` to any visit endpoint (nested or global)
  - When `description` is null, empty, blank or over 255 characters
  - Then the API responds 400 with a validation error, and no row is written
  - And the same applies when `date` is null or in the future, or when `vetId` is missing on create
  - Given `vetId` refers to a vet that does not exist
  - Then the API responds 404 with `VET_NOT_FOUND` (ERR-0005)

- [ ] **AC8: Deleting a vet does not destroy clinical history**
  - Given Dr. Carter attended three visits
  - When Dr. Carter is deleted via `DELETE /v1/vets/{id}`
  - Then the delete succeeds, all three visits still exist with their date and description intact,
    and they now render as "Seen by — not recorded"

- [ ] **AC9: Loading, empty and failure states**
  - Given a request is in flight, Then the spinner card matching the surrounding page is shown
  - Given an owner has pets but no visits, Then "No visits recorded yet" with a stethoscope icon
  - Given a search or vet filter yields nothing, Then "No visits match your search"
  - Given any visit request fails, Then a `toast.error(...)` appears and the empty state renders
    instead of a blank page

- [ ] **AC10: Seed data demonstrates the feature**
  - Given the `dev` profile (Liquibase context `tst`)
  - When the app starts
  - Then the seeded visits span several owners and pets, most have an attending vet assigned, and
    at least one is deliberately left unassigned so the "not recorded" state is visible in dev
  - And the existing `202507101201-TST-seed-data.xml` is **not modified**

## 4. Technical Requirements

- **API Changes**
  - `Visit.java` gains `@ManyToOne @JoinColumn(name = "vet_id") private Vet vet;` — **nullable**,
    mirroring the existing `Visit.pet` association at `Visit.java:43-48`. One attending vet per
    visit; deliberately not a `visit_vets` join table, which would add eager-fetch and mapping
    complexity for a rare case, and deliberately not a free-text "seen by" string, which would lose
    the FK and make "all visits by Dr. Carter" unanswerable.
  - `CreateVisitRequest` gains `vetId` (`@Schema`, REQUIRED); `UpdateVisitRequest` gains `vetId`
    (`@Schema`, NOT_REQUIRED). Required on create because recording who attended is the point of
    the story; optional on update so an old, un-attributed visit can still have a typo fixed
    without forcing someone to invent an attending vet years after the fact.
  - `VisitResponse` gains `petName`, `ownerId`, `ownerFirstName`, `ownerLastName`, `vetId`,
    `vetFirstName`, `vetLastName` — **flat fields, not a nested object**, matching the
    `ownerFirstName`/`ownerLastName` precedent added to `PetResponse.java` by
    `.claude/completed/pets-lookup-page.md`.
  - `pet/model/response/VisitSummaryResponse.java` gains `vetId`, `vetFirstName`, `vetLastName`
    (this is what the pet detail pages actually render, via `PetResponse.visits`).
  - `VisitMapper.java` gains `@Mapping`s off `vet.id` / `vet.firstName` / `vet.lastName` and
    `pet.name` / `pet.owner.*`; `PetMapper.toVisitSummaryResponse` gains the vet ones. MapStruct
    generates null-safe nested access, so a null `vet` maps to null fields, not an NPE.
  - **New endpoint**: `GET /v1/owners/{ownerId}/visits` → `List<VisitResponse>`, added to the
    existing nested `VisitController.java` (which already owns
    `/owners/{ownerId}/pets/{petId}/visits`). New constant in `Paths.java`:
    `OWNER_VISITS = BASE_PATH + "/owners/{ownerId}" + VISITS_PART` — reuses the existing
    `VISITS_PART` at `Paths.java:24`. 404s with `OWNER_NOT_FOUND` for an unknown owner.
    Chosen over looping `getVisitsByPet` per pet in the browser, which would be N+1 requests and
    against the thin-wrapper convention.
  - `VisitRepository.java` gains `findByPetOwnerIdOrderByDateDesc(Integer ownerId)` and
    `findByPetIdOrderByDateDesc(Integer petId)` (the latter replaces `findByPetId`).
  - `VisitService` injects `VetRepository` (to resolve `vetId`, throwing
    `DataNotFoundException(VET_NOT_FOUND)`) and `OwnerRepository` (to guard the new endpoint).
    The `vet` assignment is a **manual field copy** in `create`/`update` — mapping stays asymmetric.
  - No new `ApiErrorCode` value: `VET_NOT_FOUND` (ERR-0005) and `OWNER_NOT_FOUND` (ERR-0002)
    already exist.

- **Database**
  Two **new** changesets in `server/src/main/resources/db/changelog/changesets/`. The master
  changelog uses `includeAll`, so no registration step is needed. **No existing changeset is edited.**
  1. `202608240900-PRD-add-vet-to-visits.xml` (no `context` — always runs):
     `ALTER TABLE visits ADD COLUMN vet_id INTEGER;` plus
     `ALTER TABLE visits ADD CONSTRAINT fk_visits_vet FOREIGN KEY (vet_id) REFERENCES vets (id)
      ON DELETE SET NULL;`
     Nullable is mandatory, not stylistic: existing rows have no vet and H2 rejects a `NOT NULL`
     FK added to a populated table. `ON DELETE SET NULL` is what makes AC8 work — clinical history
     must outlive staff turnover, and the UI already renders "— not recorded". (Blocking the delete
     with a 409 was rejected: it needs a new exception path and error code, and stops legitimate
     admin cleanup. Doing nothing is not an option — `VetService.delete` would start returning 500
     the moment a vet has any visit.)
  2. `202608240901-TST-visit-vet-assignments.xml` (`context="tst"`): `UPDATE`s the four seeded
     visits to assign vets (leaving one NULL), `INSERT`s additional visits across more owners/pets
     so the history views are demonstrable, and re-runs
     `ALTER TABLE visits ALTER COLUMN id RESTART WITH <n>` since
     `202507101201-TST-seed-data-8` already restarted it at 5. Today's 4 seeded visits sit across
     2 pets belonging to the same owner, which demos the roll-up and the vet filter poorly.

- **Security**
  Unchanged. `anyRequest().permitAll()` in `WebSecurityConfig` stays exactly as-is, per AGENTS.md.
  This story is **staff-facing only** — owners get their history by asking staff, who read it off
  these screens. No owner login, no accounts, no per-owner scoping; see Out of Scope.
  Input validation moves from "none" to a new `visit/model/validator/VisitValidator.java`
  implementing `Validator<Object>` in the shape of `owner/model/validator/OwnerValidator.java`,
  called as `visitValidator.validateAndThrow(request)` on the **first line of all four mutating
  endpoints** (create/update on both `VisitController` and `VisitGlobalController`). No Jakarta
  Bean Validation annotations are added — the `// BUG: No @NotBlank` comment at `Visit.java:38`
  points the wrong way for this codebase and should be replaced with a pointer to `VisitValidator`.

- **Performance**
  `Pet.visits` is `FetchType.EAGER` and `Visit.vet` (`@ManyToOne`) defaults to eager, so
  `GET /v1/pets` now loads a vet per visit. Acceptable at workshop/clinic scale; flagged so it is
  a known trade-off rather than a surprise. Ordering is done in the database
  (`OrderByDateDesc` / `@OrderBy("date DESC")` on `Pet.visits`), not in the Svelte template.
  Search and vet filtering on `/visits` stay client-side `$derived` over the fetched array — no
  per-keystroke requests. Explicit non-requirement: no pagination.

## 5. Design & UI/UX

**A. Pet visit history — `/pets/[id]` and `/owners/[id]/pets/[petId]` (AC4)**
```
  Visit History                                              [ + Add Visit ]
  ┌──────────────────────────────────────────────────────────────────────────┐
  │  ┌────┐                                                                  │
  │  │ 🩺 │  Spayed                                        September 4, 2023 │
  │  └────┘  Seen by Dr. Helen Leary                                         │
  └──────────────────────────────────────────────────────────────────────────┘
  ┌──────────────────────────────────────────────────────────────────────────┐
  │  ┌────┐                                                                  │
  │  │ 🩺 │  Rabies shot                                     January 1, 2023 │
  │  └────┘  Seen by — not recorded                                          │
  └──────────────────────────────────────────────────────────────────────────┘
```
Same `Card.Root` + `Stethoscope`-in-a-circle structure already at
`client/src/routes/pets/[id]/+page.svelte:126-142`; only the "Seen by" line is new.
`+ Add Visit` appears on the nested (owner) page only, as today.

**B. Global visits list — `/visits` (AC5)**
```
  ┌────┐
  │ 📅 │   Visits
  └────┘   View all veterinary visits across all pets

  ┌──────────────────────────────────────────┐   ┌──────────────────┐
  │ 🔍  Search by pet, owner, vet or date... │   │ All vets       ▾ │
  └──────────────────────────────────────────┘   └──────────────────┘

  ┌───────────┬──────────────┬───────────┬───────────────┬──────────────────┬──────┐
  │ Date      │ Description  │ Pet       │ Owner         │ Seen by          │      │
  ├───────────┼──────────────┼───────────┼───────────────┼──────────────────┼──────┤
  │ 04 Sep 23 │ Spayed       │ Samantha  │ Jean Coleman  │ Dr. Helen Leary  │ View │
  │ 04 Jun 23 │ Neutered     │ Max       │ Jean Coleman  │ Dr. James Carter │ View │
  │ 01 Jan 23 │ Rabies shot  │ Samantha  │ Jean Coleman  │ —                │ View │
  └───────────┴──────────────┴───────────┴───────────────┴──────────────────┴──────┘

  Showing 3 of 3 visits
```
Pet cell links to `/pets/{petId}`, Owner cell to `/owners/{ownerId}`, Seen by to `/vets/{vetId}`.
`View` becomes an enabled `ExternalLink` button to `/pets/{petId}` — replacing the
`disabled title="coming soon"` button at `client/src/routes/visits/+page.svelte:103`.
The vet dropdown copies the pet-type filter pattern from `client/src/routes/pets/+page.svelte`.

**C. Owner-level roll-up — `/owners/[id]` (AC6)**
```
  Pets                                                          [ + Add Pet ]
  ┌───────────────┐  ┌───────────────┐
  │ 🐾 Samantha   │  │ 🐾 Max        │      ... existing pet cards, unchanged
  └───────────────┘  └───────────────┘

  Visit History                                          across all of this owner's pets
  ┌──────────────────────────────────────────────────────────────────────────┐
  │ 🩺  Spayed                        ( Samantha )              04 Sep 2023   │
  │     Seen by Dr. Helen Leary                                              │
  ├──────────────────────────────────────────────────────────────────────────┤
  │ 🩺  Neutered                      ( Max )                   04 Jun 2023   │
  │     Seen by Dr. James Carter                                             │
  ├──────────────────────────────────────────────────────────────────────────┤
  │ 🩺  Rabies shot                   ( Samantha )              01 Jan 2023   │
  │     Seen by — not recorded                                               │
  └──────────────────────────────────────────────────────────────────────────┘

  ── empty ──                              ── loading ──
  ┌─────────────────────────────┐          ┌─────────────────────────────┐
  │            🩺               │          │        ( spinner )          │
  │  No visits recorded yet     │          │     Loading visits...       │
  └─────────────────────────────┘          └─────────────────────────────┘
```
Pet name is a `<Badge variant="secondary">` linking to `/owners/{ownerId}/pets/{petId}`, matching
the badge usage at `client/src/routes/owners/[id]/+page.svelte:180`.

**D. Record visit form — `/owners/[id]/pets/[petId]/visits/new` (AC3)**
```
  ← Back to Pet

  Record New Visit
  Add a new visit record for this pet

  ┌────────────────────────────────────────────────────────────┐
  │  Visit Date                                                │
  │  ┌──────────────────────────────────────────────────────┐  │
  │  │ 2026-08-24                                        📅 │  │
  │  └──────────────────────────────────────────────────────┘  │
  │                                                            │
  │  Seen by                                                   │
  │  ┌──────────────────────────────────────────────────────┐  │
  │  │ Select the attending vet                           ▾ │  │
  │  └──────────────────────────────────────────────────────┘  │
  │                                                            │
  │  Description                                               │
  │  ┌──────────────────────────────────────────────────────┐  │
  │  │ Describe the reason for the visit...                 │  │
  │  │                                                      │  │
  │  └──────────────────────────────────────────────────────┘  │
  │                                                            │
  │                       [ Cancel ]  [ Record Visit ]         │
  └────────────────────────────────────────────────────────────┘
```
The `Select` is `Select.Root type="single"` driven by `getVets()`, structurally identical to the
pet-type select at `client/src/lib/components/pets/PetForm.svelte:80-95`. `Record Visit` is
disabled until a vet is chosen, mirroring `PetForm.svelte:102`.

## 6. Implementation Notes

- **Existing code to reuse**
  - `server/src/main/java/dev/ilionx/workshop/api/owner/model/validator/OwnerValidator.java` — copy
    verbatim as the shape for the new `VisitValidator`: `implements Validator<Object>`, public
    `String` constants for messages and field names, `result.rejectField(...).whenNull(...).orWhen(...)`,
    `throw new ValidationException(result)` if `result.hasErrors()`, and an `instanceof` dispatch
    `validate(Object, ValidationResult)`.
  - `server/src/main/java/dev/ilionx/workshop/api/pet/model/mapper/PetMapper.java:22-33` — the exact
    `@Mapping(source = "owner.firstName", target = "ownerFirstName")` pattern to copy for the vet
    and pet/owner fields on `VisitMapper`.
  - `server/src/main/java/dev/ilionx/workshop/api/visit/controller/VisitController.java:50-57` — the
    nested-controller shape (`petService.findByIdAndOwnerId` guard, then service call, then
    `visitMapper.toResponseList`) for the new `getVisitsByOwner`.
  - `client/src/routes/pets/+page.svelte` — the search input + filter dropdown + `Table.Root` +
    "Showing N of M" combination to copy onto `/visits`.
  - `client/src/lib/components/pets/PetForm.svelte:78-96` — the `Select` block for the vet dropdown.
  - `client/src/lib/api/vet/VetController.ts:17` — `getVets()` already exists, call it unchanged.
  - `client/src/routes/pets/[id]/+page.svelte:126-142` — the visit card markup to extend.
  - `server/src/test/java/dev/ilionx/workshop/support/IntegrationTest.java:98` — `aSavedVisit(Pet)`
    needs an `aSavedVisit(Pet, Vet)` overload; `vetRepository` is already injected there, and the
    teardown order (visits → pets → owners, vets last) already works with the new FK.

- **New code needed**
  - `server/src/main/java/dev/ilionx/workshop/api/visit/model/validator/VisitValidator.java` — first
    validator in this domain.
  - `server/src/main/resources/db/changelog/changesets/202608240900-PRD-add-vet-to-visits.xml`
  - `server/src/main/resources/db/changelog/changesets/202608240901-TST-visit-vet-assignments.xml`
  - `server/src/test/java/dev/ilionx/workshop/api/visit/model/validator/VisitValidatorTest.java` —
    extends `UnitTest` (Mockito, no Spring context) — it is pure logic.
  - `client/src/lib/api/visit/VisitController.ts` — add a `getVisitsByOwner(ownerId)` wrapper.
  - Modified, not new: `Visit`, `CreateVisitRequest`, `UpdateVisitRequest`, `VisitResponse`,
    `VisitSummaryResponse`, `VisitMapper`, `PetMapper`, `VisitRepository`, `VisitService`,
    `VisitController`, `VisitGlobalController`, `Paths`, plus `/visits`, `/pets/[id]`,
    `/owners/[id]`, `/owners/[id]/pets/[petId]`, `/owners/[id]/pets/[petId]/visits/new`.

- **Patterns to follow (AGENTS.md)**
  - **Asymmetric mapping**: MapStruct is entity→response only. Resolving `vetId` → `Vet` is a manual
    lookup + `visit.setVet(vet)` inside `VisitService.create`/`update`. Do **not** add a
    request→entity mapper method.
  - **DTO separation**: `CreateVisitRequest` and `UpdateVisitRequest` are edited independently
    (`vetId` is REQUIRED on one, NOT_REQUIRED on the other). No entity leaves a controller.
  - **Validation via `Validator` component**, never Jakarta annotations on the entity or DTOs.
    `validateAndThrow` is the first line of every mutating endpoint — all four of them.
  - **Liquibase**: two brand-new changesets, `<timestamp>-<CONTEXT>-<description>.xml`. Never touch
    `202507101200-PRD-initial-schema.xml` or `202507101201-TST-seed-data.xml`. The schema changeset
    carries **no** `context`; the seed one carries `context="tst"`. `db.changelog-master.yaml` uses
    `includeAll`, so dropping the files in is enough.
  - **Frontend fetching**: thin wrapper in `VisitController.ts`, called from a component `$effect`.
    No SvelteKit `load` functions — this app has none.
  - Run `cd client && bun run sync:api` after the DTO changes; never hand-edit
    `client/src/lib/types/api.d.ts`. `models.ts` already re-exports `VisitResponse` and
    `VisitSummaryResponse`, so new fields flow through with no edit there.
  - `bun`, not `npm`. Let Spotless format the Java on build rather than hand-formatting.
  - Testing: `VisitValidatorTest` extends `UnitTest`; endpoint changes extend the existing
    `IntegrationTest`-based `VisitControllerTest` / `VisitGlobalControllerTest` rather than adding
    new classes. Add an integration test for AC8 (delete a vet, assert visits survive with a null vet).
  - Verify with `cd server && ./gradlew test` and `cd client && bun run check`, plus a manual pass
    over all four UI surfaces.
  - Replace the `// BUG: No @NotBlank validation` comment at `Visit.java:38` with a pointer to
    `VisitValidator` — leave the `// BUG: No @Past` comment in `Pet.java:44` alone, it is a separate
    workshop exercise.

## 7. Out of Scope
- **An owner-facing self-service portal.** No owner login, no accounts, no per-owner data scoping.
  `anyRequest().permitAll()` stays. Owners get their history by asking staff, who read it off these
  screens. A real portal is its own epic (authentication, owner credentials, authorization rules).
- Multiple vets per visit — one attending vet only.
- Retroactively attributing the vet on real historical visits (they stay "not recorded"; only *seed*
  data gets back-filled, for demo purposes).
- Any richer clinical record: diagnosis codes, prescriptions, weight, attachments, billing.
- Editing or deleting visits from `/visits` or the owner roll-up — those stay read-only surfaces;
  visit CRUD remains owner→pet-first.
- Server-side search, sorting or pagination on visit endpoints.
- A per-vet "my caseload" page (`/vets/[id]` listing that vet's visits). Now *possible* thanks to the
  new FK, but not built here — worth a follow-up story.
- Printing or exporting a visit history.
- The `@Past` birth-date bug on `Pet` (`Pet.java:44`).
