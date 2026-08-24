# Changelog

| Date | Feature | Summary |
| ---- | ------- | ------- |
| 2026-08-24 | Pets lookup & detail pages | Added `/pets` (searchable, filterable list) and `/pets/[id]` (read-only detail) pages; `PetResponse` gained owner name fields via `PetMapper`. See `.claude/completed/pets-lookup-page.md`. |
| 2026-08-24 | Theme selector (Light/Dark/Pawsome) | Added a header dropdown to switch between three themes via CSS custom-property overrides in `app.css`, persisted to `localStorage`, applied pre-hydration to avoid a flash of the wrong theme. Fixed a pre-existing bug where `dark:` utilities silently followed OS `prefers-color-scheme` instead of the selected theme. See `.claude/completed/theme-switcher.md`. |
