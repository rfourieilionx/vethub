---
epic: "PET-MANAGEMENT"
title: "Pets Lookup Page"
estimate: S
status: ready
created: 2026-08-24
depends_on: []
labels: [backend, frontend]
priority: P2
---

## 1. User Story
**As a** receptionist at the clinic
**I want** a Pets page that lists every patient with search and type filtering
**So that** I can find a pet by its name when a caller doesn't remember which owner record it sits under

## 2. Business Context & Value
The home page advertises a Pets tile that leads nowhere — a visible broken promise in the product
(`client/src/routes/+page.svelte:49` links to `/pets`, and no `client/src/routes/pets/` route exists,
so the click 404s).

Today, finding a patient means guessing the owner first: Owners -> search -> open owner -> find pet.
On a phone call ("this is about Leo, he was in last week") the receptionist often has the pet's name
and nothing else. A flat, searchable patient list removes a two-hop detour from the single most
frequent front-desk task, and closes the dead link.

Note on epic/priority/estimate: `PET-MANAGEMENT` / P2 / S are placeholders — no project board was
supplied at refinement time. Replace with the real epic key and sizing before sprint planning.

**Amendment (exercise 04, build time):** the "standalone `/pets/[id]`" line originally in Out of
Scope has been reversed. Exercise 04's acceptance criteria required a `/pets/[id]` route, so a new
**read-only** detail page was built instead of routing into the nested owner/pet page — see AC10.
CRUD still stays owner-first as originally scoped; the new page has no edit/delete/add-visit actions.
This story has been implemented and verified (`bun run check` clean, backend tests passing, routes
confirmed live).

## 3. Acceptance Criteria
- [ ] **AC1: Pets page exists and is reachable**
  - Given I am on the VetHub home page
  - When I click the "Pets" tile
  - Then I land on `/pets` and see a list of all pets, not a 404
- [ ] **AC2: Pets appears in the main navigation**
  - Given I am on any page
  - When I look at the header nav
  - Then "Pets" appears between "Owners" and "Veterinarians" and is highlighted while I am on `/pets`
- [ ] **AC3: Each row identifies the patient and its owner**
  - Given pets exist
  - When the list renders
  - Then each row shows pet name, type, age (derived from `birthDate`), owner full name, and last visit date
- [ ] **AC4: Search matches pet name, owner name, and type**
  - Given the list is loaded
  - When I type "leo" or "franklin" or "cat" into the search box
  - Then only matching rows remain, case-insensitively, and the footer reads "Showing N of M pets"
- [ ] **AC5: Type filter**
  - Given the type dropdown is populated from `GET /v1/pet-types`
  - When I select "Cat"
  - Then only cats remain, combined with any active search term
- [ ] **AC6: Click through to the pet**
  - Given a row for pet 7 belonging to owner 4
  - When I click the pet name or "View"
  - Then I navigate to `/pets/7` (amended — see AC10; originally scoped to route into the nested
    owner/pet page instead)
- [ ] **AC7: Loading and empty states**
  - Given the request is in flight, Then a spinner card is shown (matching `/owners`)
  - Given no pets exist at all, Then "No pets registered yet" with a paw icon
  - Given a search yields nothing, Then `No pets found matching "<query>"`
- [ ] **AC8: Failure is visible, not silent**
  - Given `GET /v1/pets` fails
  - When the page loads
  - Then a `toast.error('Failed to load pets')` appears and the empty state renders instead of a blank page
- [ ] **AC9: Owner name is served by the API**
  - Given `GET /v1/pets`
  - When the response is returned
  - Then each `PetResponse` includes `ownerFirstName` and `ownerLastName` alongside the existing `ownerId`
- [ ] **AC10: Read-only pet detail page**
  - Given a valid pet ID
  - When I navigate to `/pets/{id}` (directly, or via AC6)
  - Then I see the pet's name, type, age, birth date, an owner link to `/owners/{ownerId}`, and its visit
    history, sorted most-recent-first
  - And there are no edit, delete, or "Add Visit" controls on this page — CRUD stays owner-first
  - Given an invalid/non-existent pet ID, Then a "Pet not found" empty state renders instead of an error

## 4. Technical Requirements
- **API Changes**: No new endpoints. `GET /v1/pets` (`PetGlobalController.getAllPets`,
  `server/src/main/java/dev/ilionx/workshop/api/pet/controller/PetGlobalController.java:48`) and
  `GET /v1/pet-types` are reused as-is. Only `PetResponse`
  (`server/src/main/java/dev/ilionx/workshop/api/pet/model/response/PetResponse.java`) gains two fields,
  `ownerFirstName` / `ownerLastName`, each with `@Schema` metadata; `PetMapper`
  (`server/src/main/java/dev/ilionx/workshop/api/pet/model/mapper/PetMapper.java`) gains two explicit
  `@Mapping`s off `owner.firstName` / `owner.lastName` next to the existing `owner.id -> ownerId`.
  `server/src/main/java/dev/ilionx/workshop/api/Paths.java` is untouched — `PETS` already exists at line 48.
- **Database**: None. No entity field is added, so no Liquibase changeset is required. (`Pet.owner`
  already exists; this is purely a projection change.) No existing changeset is edited.
- **Security**: Unchanged. `anyRequest().permitAll()` in `WebSecurityConfig` stays as-is per AGENTS.md —
  this story does not touch the security posture. No mutating endpoint is added, so no `PetValidator`
  work is needed (validators are only invoked from mutating endpoints).
- **Performance**: `PetService.findAll()` over the seeded dataset is fine at workshop scale. Search and
  type filtering are client-side `$derived` over the already-fetched array — no per-keystroke requests.
  Explicit non-requirement: no pagination (see Out of Scope).

## 5. Design & UI/UX

```
┌───────────────────────────────────────────────────────────────────────────────────────┐
│  [VetHub]        Home   Owners   ▸Pets◂   Veterinarians   Visits                       │
└───────────────────────────────────────────────────────────────────────────────────────┘

  ┌────┐
  │ 🐾 │   Pets
  └────┘   Look up any patient across all owners

  ┌──────────────────────────────────────────┐   ┌──────────────────┐
  │ 🔍  Search by pet, owner, or type...     │   │ All types      ▾ │
  └──────────────────────────────────────────┘   └──────────────────┘

  ┌───────────────────────────────────────────────────────────────────────────────────┐
  │ Name        │ Type      │ Age            │ Owner             │ Last visit │        │
  ├───────────────────────────────────────────────────────────────────────────────────┤
  │ Leo         │ ( Cat )   │ 5 years old    │ George Franklin   │ 09 Dec 25  │ View > │
  │ Basil       │ ( Hamst ) │ 2 years old    │ Eduardo Rodriquez │ —          │ View > │
  │ Rosy        │ ( Dog )   │ 3 years old    │ Jeff Black        │ 14 Jan 26  │ View > │
  │ Iggy        │ ( Lizard )│ < 1 month old  │ Maria Escobito    │ —          │ View > │
  └───────────────────────────────────────────────────────────────────────────────────┘

  Showing 4 of 13 pets

  ── empty state (no pets at all) ──          ── empty state (no search match) ──
  ┌─────────────────────────────────┐         ┌─────────────────────────────────┐
  │             🐾                  │         │             🐾                  │
  │   No pets registered yet        │         │  No pets found matching "zzz"   │
  └─────────────────────────────────┘         └─────────────────────────────────┘

  ── loading state ──
  ┌─────────────────────────────────┐
  │           ( spinner )           │
  │        Loading pets...          │
  └─────────────────────────────────┘
```

Notes:
- Pet name and "View" both link to `/pets/{id}` (amended, see AC10); owner name links to `/owners/{ownerId}`.
- Type is rendered as `<Badge variant="secondary">`, matching the pet badges in the Owners table
  (`client/src/routes/owners/+page.svelte:131`).
- "Last visit" = `max(visit.date)` from the `visits` array already present on `PetResponse`; em dash when empty.
- Age reuses the `calculateAge()` logic from the pet detail page.
- Header, search input, table, footer count and card styling are copied from `/owners` so the two pages
  are visually indistinguishable.

**`/pets/[id]` detail page (AC10):**
```
┌───────────────────────────────────────────────────────────────────────────────────────┐
│  ← Back to Pets                                                                        │
│                                                                                          │
│  ┌────┐  Leo                                                                            │
│  │ 🐾 │  ( Cat )  •  5 years old                                                        │
│  └────┘                                                                                  │
│         📅 Born: September 7, 2020                                                      │
│         🐾 Owner: George Franklin  (links to /owners/1)                                 │
│                                                                                          │
│  Visit History                                                                          │
│  ┌────────────────────────────────────────────────────────────────────────────────┐    │
│  │ 🩺  Rabies shot                                                    Jan 1, 2023  │    │
│  └────────────────────────────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────────────────────────┘
```
No Edit/Delete buttons, no "Add Visit" button — view-only, modeled on the nested pet detail page's
card structure but with those actions stripped out.

## 6. Implementation Notes
- Existing code to reuse:
  - `client/src/routes/owners/+page.svelte` — copy its page skeleton verbatim: header block, search
    `Input` with absolutely-positioned `Search` icon, `Table.Root`, spinner card, empty card, and the
    "Showing X of Y" footer.
  - `client/src/lib/api/pet/PetController.ts:17` — `getPets()` already exists, call it unchanged.
  - `client/src/lib/api/pet-type/PetTypeController.ts:17` — `getPetTypes()` for the filter dropdown.
  - `client/src/routes/owners/[id]/pets/[petId]/+page.svelte:65` — lift `calculateAge()` as-is.
  - `client/src/routes/visits/+page.svelte` — precedent for a global, cross-domain read-only list page.
- New code needed:
  - `client/src/routes/pets/+page.svelte` — the list page.
  - `client/src/routes/pets/[id]/+page.svelte` — the read-only detail page (AC10), fetching via
    `getPetById(id)` from `PetController.ts` (already existed, no new API code).
  - `client/src/lib/components/layout/Header.svelte` — add `{ href: '/pets', label: 'Pets', icon: PawPrint }`
    to `navItems` between Owners and Veterinarians; `isActive()` already handles it via `startsWith`.
  - `server/src/main/java/dev/ilionx/workshop/api/pet/model/response/PetResponse.java` — two new fields.
  - `server/src/main/java/dev/ilionx/workshop/api/pet/model/mapper/PetMapper.java` — two new `@Mapping` entries.
  - `server/src/test/java/dev/ilionx/workshop/api/pet/controller/PetGlobalControllerTest.java` — extend the
    existing `GET /v1/pets` assertions to cover `ownerFirstName` / `ownerLastName`.
- Patterns to follow (AGENTS.md):
  - Mapping is asymmetric — MapStruct handles entity→response only. These two `@Mapping`s are exactly
    that; nothing request→entity is added.
  - DTO separation preserved: only a response DTO changes, no request DTO touched and no entity leaks
    out of a controller.
  - No Jakarta Bean Validation annotations, and no `PetValidator` change — validators are only invoked
    from mutating endpoints, and this story adds none.
  - No Liquibase changeset — there is no schema change, and no existing changeset is edited.
  - Run `cd client && bun run sync:api` after the `PetResponse` change; never hand-edit
    `client/src/lib/types/api.d.ts`. No new re-export is needed in `client/src/lib/api/models.ts:19` —
    it already re-exports `PetResponse`, so the new fields flow through automatically.
    Note: `scripts/common.sh`'s `HEALTH_URL` was pointing at `/api/actuator/health`, which 404s —
    the real path (given `management.endpoints.web.base-path: /v1/public/actuator`) is
    `/api/v1/public/actuator/health`. This was fixed as part of this story since it silently broke
    `sync:api` for everyone; worth knowing if it regresses.
  - Data fetching is client-side inside `$effect` — no SvelteKit `load` function (this app has none).
  - Use `bun`, not `npm`, in `client/`. Let Spotless format the Java on build rather than hand-formatting.
  - Test with the lightest base class that fits — the mapper change is covered by the existing
    `IntegrationTest`-based `PetGlobalControllerTest`; no new `UnitTest` is needed.
  - Verify with `cd client && bun run check` and `cd server && ./gradlew test`.

## 7. Out of Scope
- Creating, editing or deleting pets from this page (or from `/pets/[id]`) — pet creation still starts
  from an owner, because `CreatePetRequest` requires an `ownerId` and no owner-picker component exists.
- Server-side search, sorting or pagination on `GET /v1/pets`.
- Column sorting.
- Any change to the security posture (`anyRequest().permitAll()` stays).
- The disabled "coming soon" action button on `/visits` (`client/src/routes/visits/+page.svelte:103`) —
  unrelated, tracked separately.
