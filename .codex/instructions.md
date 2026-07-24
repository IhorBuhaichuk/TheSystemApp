# Codex Project Boot Instructions

Project: **THE SYSTEM: LEVEL UP**.

Use this as the fast-start rule set for every coding session in this repository.

## First Context Read

- For refactors, feature work, Room changes, navigation changes, or architecture questions, read `GRAPH_REPORT.md` first.
- After `GRAPH_REPORT.md`, read only the directly relevant files: ViewModel, domain use case, repository interface/implementation, DAO/entity, navigation route, or Compose screen.
- For simple tasks like copy edits, color token tweaks, typo fixes, and constants, use targeted reads only.

## Architecture

- `:domain` owns pure Kotlin models, repository contracts, policies, and use cases.
- `:app` owns Android, Compose, Room, Hilt, workers, Health Connect, AI SDK clients, and repository implementations.
- Do not put Android, Room, Compose, resource, or Hilt UI dependencies into `:domain`.
- Keep AI output behind domain validation before it changes workouts, plans, quests, or progression.

## UI

- For UI work, read `UI_UX_GUIDELINES.md` first.
- UI must be native Kotlin + Jetpack Compose only.
- Never generate React, HTML, CSS, Tailwind, Framer Motion, 21st.dev, or web UI/library code for this app.
- Use `SystemTheme`, shared tokens, `SystemPanel`, `techSurface`, `SystemButton`, and existing shared Compose primitives before adding new styling.

## Default Workflow

- Architect: decide the layer and minimal change path.
- Coder: implement with existing Kotlin/Compose/Hilt/Room patterns.
- Reviewer: check regressions, missing UI blocks, logging, validation, Room migration/schema, and domain/app boundaries.

## Verification

- Documentation-only changes: confirm no `.kt` / `.kts` files changed.
- Kotlin-only UI or ViewModel changes: run `scripts/check-quick.cmd`.
- Broad behavior changes: run `scripts/check-tests.cmd` or a narrower targeted Gradle test.
- Room changes: run `scripts/check-room.cmd` and update `GRAPH_REPORT.md` if schema/DAO/database shape changes.
