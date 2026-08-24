---
epic: "UX-POLISH"
title: "Theme Switcher"
estimate: M
status: ready
created: 2026-08-24
depends_on: []
labels: [frontend]
priority: P3
---

## 1. User Story
**As a** clinic staff member using VetHub through a long shift
**I want** to pick between a Light, Dark and a playful "Pawsome" theme from the header
**So that** the app is comfortable to look at in my lighting conditions and feels less plain

## 2. Business Context & Value
VetHub currently ships a single hardcoded light palette (`client/src/app.css:11-76`). Front-desk staff
work under bright fluorescents; vets checking records in a dim exam room or on evening shift get a
full-white screen with no alternative. There is no theme mechanism of any kind in the client today —
grepping `client/src` for `theme|dark|localStorage` turns up only a static `<meta name="theme-color">`
and shadcn's unused `dark:` utility classes.

The groundwork is already done: `app.css` defines ~40 CSS custom properties and re-exports them through
`@theme inline` (`app.css:79-126`), and every component consumes tokens (`bg-card`, `text-muted-foreground`)
rather than literal colors. Adding themes is therefore an override-block problem, not a repaint — which is
why this is M and not L.

Secondary value: it fixes a live rendering bug. shadcn components carry `dark:` utilities but `app.css`
never declares `@custom-variant dark`, so Tailwind v4's default (`prefers-color-scheme`) makes them fire
on OS-dark machines while the palette stays light.

Note: `UX-POLISH` / P3 / M are placeholders — no project board was supplied at refinement time. Replace
with the real epic key and sizing before sprint planning. M is driven by the 16-route x 3-theme manual
sweep and the token-extraction work, not by the CSS itself.

## 3. Acceptance Criteria
- [ ] **AC1: Theme selector is visible on every page**
  - Given I am on any route in the app
  - When I look at the header
  - Then a palette-icon button sits to the right of the nav, on both desktop and mobile
- [ ] **AC2: Three themes are offered**
  - Given I open the selector
  - When the menu renders
  - Then I see exactly "Light", "Dark" and "Pawsome", each with an icon and a colour-swatch preview,
    and the active one is marked with a check
- [ ] **AC3: Light is pixel-identical to today**
  - Given I select "Light"
  - When any page renders
  - Then it is visually indistinguishable from the app before this story shipped
- [ ] **AC4: Switching is instant and total**
  - Given I select a different theme
  - When the menu closes
  - Then the whole app repaints without a page reload — header, footer, cards, tables, forms, badges,
    buttons, dialogs, dropdowns and toasts all follow, with no element left on the previous palette
- [ ] **AC5: Choice survives a refresh**
  - Given I picked "Dark"
  - When I hard-refresh, open a new tab, or return the next day
  - Then Dark is still applied
- [ ] **AC6: No flash of the wrong theme**
  - Given my stored theme is "Dark"
  - When the page first paints
  - Then it paints dark immediately — no white flash before hydration
- [ ] **AC7: Sensible first-visit default**
  - Given I have never chosen a theme
  - When I open the app with my OS set to dark
  - Then Dark is applied; with the OS set to light (or no preference expressed), Light is applied
  - And Pawsome is never auto-selected — it is opt-in only
- [ ] **AC8: Corrupt or stale storage degrades safely**
  - Given `localStorage` holds `"neon"`, is empty, or throws (private browsing / disabled)
  - When the app loads
  - Then Light renders and nothing errors in the console
- [ ] **AC9: Every theme overrides tokens, not components**
  - Given a new theme is added later
  - When a developer implements it
  - Then it is a single token block in `app.css` — no `.svelte` file contains a per-theme conditional
- [ ] **AC10: Pawsome differs beyond colour**
  - Given I select "Pawsome"
  - When any page renders
  - Then corner radius, border weight, shadow colour, heading font and the page background gradient all
    change too — it is not merely a hue shift
- [ ] **AC11: Dark is readable**
  - Given Dark is active
  - When I inspect body text, muted text, and primary buttons
  - Then each meets WCAG AA contrast (4.5:1 body, 3:1 large text/UI), and no `dark:`-prefixed utility
    fires on a non-Dark theme
- [ ] **AC12: Keyboard and screen-reader accessible**
  - Given I am navigating by keyboard
  - When I tab to the selector
  - Then it is focusable, opens with Enter/Space, is arrowable, closes with Escape, exposes an
    `aria-label` ("Select theme") and marks the active item with `aria-checked`

## 4. Technical Requirements
- **API Changes**: None. This is frontend-only — no controller, no DTO, no `Paths.java` entry, and
  therefore **no `bun run sync:api`** and no touch to `client/src/lib/types/api.d.ts`.
- **Database**: None. No entity changes, so **no Liquibase changeset** is created and no existing
  changeset is edited. The theme lives in `localStorage`, not on a user record (there is no user table).
- **Security**: Unchanged — `anyRequest().permitAll()` in `WebSecurityConfig` stays as-is per AGENTS.md.
  The only new attack surface is the FOUC bootstrap script in `app.html`; it must read the stored value,
  validate it against a hardcoded allowlist `['light','dark','fancy']`, and write it as a `data-theme`
  attribute — never interpolate the raw string into markup or a style. Whole script wrapped in try/catch.
- **Performance**: Theme application is a single attribute write on `<html>` — no re-render, no request.
  The bootstrap script is inline and blocking but < 400 bytes. The Pawsome display font (Baloo 2) is the
  only new network cost: one extra `family=` segment appended to the existing Google Fonts `<link>` on
  `client/src/app.html:11`, with `display=swap` preserved. Accepted trade-off: ~15-30KB is fetched even
  by users who never select Pawsome. No new npm dependency is added.

## 5. Design & UI/UX

Header, closed (desktop) — selector sits between the nav and the right edge:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│  [VetHub]     Home  Owners  Pets  Veterinarians  Visits          ( ◐ )   [≡]      │
│                                                                    ▲      ▲       │
│                                                        theme selector   mobile   │
│                                                        (always shown)  (md:hidden)│
└──────────────────────────────────────────────────────────────────────────────────┘
```

Header, selector open:

```
                                                     ( ◐ )
                                            ┌──────────────────────────┐
                                            │  Theme                   │
                                            ├──────────────────────────┤
                                            │ ✓  ☀  Light      ●●●     │
                                            │    ☾  Dark       ●●●     │
                                            │    ✨ Pawsome    ●●●     │
                                            └──────────────────────────┘
                                              ▲   ▲    ▲        ▲
                                            check icon label  3-dot swatch
                                                              (bg/primary/accent)
```

Mobile — the selector stays in the top bar, NOT hidden inside the collapsed nav,
so it is reachable without opening the menu:

```
┌────────────────────────────────────┐
│  [VetHub]              ( ◐ )  [≡]  │
└────────────────────────────────────┘
```

What each theme looks like (Owners page, same layout, three palettes):

```
── LIGHT (unchanged, today's look) ────────────────────────────────────────
  bg #FFFFFF   text #404040   primary ilionx red #E8003D   accent teal
  radius 0.625rem · 1px hairline borders · soft neutral-black shadow
  ┌──────────────────────────────────────────┐
  │ Name       │ Address    │ Pets  │        │   white card, grey hairline
  ├──────────────────────────────────────────┤
  │ G. Franklin│ 110 W Liberty│ (Leo)│ View >│
  └──────────────────────────────────────────┘

── DARK ───────────────────────────────────────────────────────────────────
  bg hsl(0 0% 9%)  card hsl(0 0% 13%)  text hsl(0 0% 92%)
  primary lifted to hsl(344 90% 58%) so red-on-dark passes AA
  radius unchanged · borders hsl(0 0% 24%) · deeper black shadow
  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
  ▓ Name       │ Address    │ Pets  │       ▓   charcoal card
  ▓───────────────────────────────────────  ▓
  ▓ G. Franklin│ 110 W Liberty│ (Leo)│View >▓
  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓

── PAWSOME ────────────────────────────────────────────────────────────────
  page background = peach → pink → lavender gradient (fixed attachment)
  primary bubblegum hsl(330 85% 58%) · accent aqua hsl(190 90% 45%)
  foreground deep plum hsl(285 35% 22%) — never pure black
  radius 1.25rem (pillowy) · 2px pink borders · pink GLOW shadow
  headings switch to Baloo 2 at weight 600
   ╭──────────────────────────────────────────╮
   │  Name       │ Address    │ Pets  │       │  ← 2px pink border,
   │─────────────────────────────────────────  │    big round corners,
   │  G. Franklin│ 110 W Liberty│(Leo)│View > │    pink glow beneath
   ╰──────────────────────────────────────────╯
      ░░░ soft pink glow ░░░
```

Reference token values (starting point, tune during build):

```
[data-theme='dark']
  --background hsl(0 0% 9%)      --foreground hsl(0 0% 92%)
  --card / --popover hsl(0 0% 13%)
  --primary hsl(344 90% 58%)     --primary-foreground hsl(0 0% 100%)
  --secondary / --muted hsl(0 0% 18%)   --muted-foreground hsl(0 0% 65%)
  --accent hsl(175 60% 45%)
  --border / --input hsl(0 0% 24%)      --ring hsl(344 90% 58%)
  --sidebar-* mirrored from the above   --app-background-image none

[data-theme='fancy']
  --radius 1.25rem               --card-border-width 2px
  --primary hsl(330 85% 58%)     --accent hsl(190 90% 45%)
  --foreground hsl(285 35% 22%)  --border hsl(330 60% 88%)
  --shadow-card        0 8px 20px -8px hsl(330 85% 58% / 0.35)
  --shadow-card-hover  0 14px 28px -10px hsl(330 85% 58% / 0.45)
  --font-display 'Baloo 2', var(--font-sans)
  --app-background-image
    linear-gradient(160deg, hsl(30 100% 97%) 0%, hsl(320 100% 97%) 45%, hsl(265 100% 97%) 100%)
```

New tokens required (things currently hardcoded that a theme must be able to move):

```
--app-background-image   none | none | linear-gradient(160deg, peach, pink, lavender)
--shadow-card            replaces the literal rgb(0 0 0 / .1) at app.css:230,237
--shadow-card-hover      replaces the literal rgb(0 0 0 / .1) at app.css:242
--card-border-width      1px  | 1px  | 2px
--font-display           inherits --font-sans for Light/Dark; Baloo 2 for Pawsome
--hero-gradient-from/to  replaces hardcoded `from-gray-50 to-gray-100` at +page.svelte:11
```

## 6. Implementation Notes
- Existing code to reuse:
  - `client/src/app.css:11-76` — the `:root` block IS the Light theme. Do not duplicate it; keep it as
    the base and add `[data-theme='dark']` / `[data-theme='fancy']` override blocks beneath it. Only
    tokens that actually differ get restated. This is what makes AC3 (Light unchanged) free.
  - `client/src/lib/components/ui/dropdown-menu/` — fully vendored (incl. `dropdown-menu-radio-group.svelte`
    and `dropdown-menu-radio-item.svelte`) and currently unused anywhere in `routes/`. Build the selector
    on `DropdownMenu.RadioGroup` — `aria-checked` and keyboard nav (AC12) come free from bits-ui.
  - `client/src/lib/components/ui/button/button.svelte` — `variant="ghost" size="icon"` is exactly the
    treatment the mobile-menu trigger uses at `client/src/lib/components/layout/Header.svelte:53-65`;
    match it so the two buttons pair visually.
  - `client/src/app.html:11` — the existing Google Fonts `<link>` is where Baloo 2 is appended (one
    `family=Baloo+2:wght@500;600;700` segment, keep `display=swap`).
  - `lucide-svelte` (already a dependency) supplies the Palette / Sun / Moon / Sparkles icons — no new
    icon package.
- New code needed:
  - `client/src/lib/theme/theme.svelte.ts` — Svelte 5 runes module: the `THEMES` list (id, label, icon,
    swatch colors), a `$state`-backed current theme, `setTheme()` writing both `document.documentElement`
    `data-theme` and `localStorage['vethub:theme']`, and a `readStoredTheme()` with allowlist validation
    (`'light' | 'dark' | 'fancy'`) + try/catch. Internal id stays `fancy` while the user-facing label is
    "Pawsome", so a later rename does not invalidate stored values. New folder; mirrors the existing
    feature-first grouping under `lib/` (`lib/api/`, `lib/config/`).
  - `client/src/lib/components/layout/ThemeSwitcher.svelte` — the dropdown, exported from
    `client/src/lib/components/layout/index.ts` alongside `Header` and `Footer`.
  - Edits:
    - `client/src/lib/components/layout/Header.svelte` — mount `<ThemeSwitcher />` in the right-hand flex
      group at line 30, *outside* the `md:hidden` mobile button so it shows at all breakpoints.
    - `client/src/app.html` — inline bootstrap script in `<head>` before `%sveltekit.head%` (reads
      `localStorage['vethub:theme']`, falls back to `matchMedia('(prefers-color-scheme: dark)')`, then to
      `light`; sets `data-theme` on `document.documentElement`), plus the Baloo 2 font segment.
    - `client/src/app.css` — the two override blocks, `@custom-variant dark`, the six new tokens, and
      swapping the hardcoded shadows at lines 230/237/242 for `var(--shadow-card)` /
      `var(--shadow-card-hover)`.
    - `client/src/routes/+layout.svelte:9` — pass `theme` to `<Toaster>` (`dark` for Dark, `light` for
      Light and Pawsome), otherwise svelte-sonner paints its own white toasts on the dark palette (AC4).
    - `client/src/routes/+layout.svelte:11` — swap `bg-background` for an `app-shell` class that paints
      both `--background` and `--app-background-image`, so the Pawsome gradient isn't covered by the
      wrapper div.
    - `client/src/routes/+page.svelte:11` — hero gradient `from-gray-50 to-gray-100` → the new tokens.
      This is the only hardcoded palette colour in all of `client/src`.
- Patterns to follow (AGENTS.md):
  - Frontend-only story: no DTO/validator/MapStruct/Liquibase work applies, and `bun run sync:api` is
    **not** run — the OpenAPI spec is untouched, so `client/src/lib/types/api.d.ts` must not change.
  - Data fetching stays client-side; this story adds no `load` function (the app has none) — the theme
    bootstrap is a plain inline script in `app.html`, not a SvelteKit hook.
  - Use `bun`, not `npm`, in `client/`. No new dependency is expected.
  - Add `@custom-variant dark (&:where([data-theme='dark'], [data-theme='dark'] *))` to `app.css`. Without
    it, Tailwind v4's default `dark` variant is `prefers-color-scheme`, so the `dark:` utilities already
    present in `button.svelte`, `input.svelte`, `textarea.svelte`, `badge.svelte`, `select-trigger.svelte`,
    `dialog-overlay.svelte` and `dropdown-menu-item.svelte` fire on OS-dark today regardless of the
    selected theme. This is required for AC3 and AC11, not optional polish.
  - Verify with `cd client && bun run check`, then a manual pass over all 16 routes in all 3 themes —
    UI-facing story, so a visual sweep is part of "done". No backend test run is needed (nothing in
    `server/` is touched).
  - Update AGENTS.md with the theming convention (token override blocks in `app.css`, `data-theme` on
    `<html>`, `localStorage['vethub:theme']`, and the `@custom-variant dark` gotcha) once merged — that
    file is project memory, not write-once.

## 7. Out of Scope
- Persisting the theme per user on the backend — there is no user/preferences table and
  `anyRequest().permitAll()` means there is no identified user to key it to.
- A theme *builder* / custom colour picker. Three fixed themes only.
- A fourth theme, or high-contrast / colour-blind-safe variants.
- Per-theme `<meta name="theme-color">` and favicon swapping (`client/src/app.html:7-8`).
- Animating the theme transition (cross-fade / view-transition).
- Restyling or re-laying-out any page. Light must stay pixel-identical; Dark and Pawsome are
  token swaps over the existing layout, not redesigns.
- Any backend or security-posture change.
