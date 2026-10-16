# AGENTS.md

Project rules for AI agents working on **THE SYSTEM: LEVEL UP**.

## Priority Context Rule

Перед виконанням будь-яких задач із рефакторингу чи додавання фіч СПОЧАТКУ читай `GRAPH_REPORT.md` для розуміння контексту замість суцільного сканування кодової бази.

Use targeted follow-up reads after `GRAPH_REPORT.md`: the relevant ViewModel, domain use case, repository contract/implementation, Room entity/DAO, or Compose file. Refresh the graph only when architecture has changed or when the report is clearly stale.

For task routing, use the relevant row in `docs/architecture/FEATURE_MAP.md`; open only that feature's entry points and tests. Check the map's audited revision against current code and local changes before relying on it. Code is the source of truth. Update affected map sections when contracts, navigation, persistence ownership, or cross-feature flows change. Keep `GRAPH_REPORT.md` compact (roughly 200 lines or less); keep detailed evidence and planning in `PROJECT_AUDIT.md` and `DEVELOPMENT_ROADMAP.md`, which are not mandatory context for small tasks. Do not mark documented defects resolved without implementation and verification evidence.

## Agent Roles

### Architect

- Reads `GRAPH_REPORT.md` before planning non-trivial work.
- Decides which layer owns the change: `:domain` for business rules, `:app/data` for persistence/integration, `:app/feature` for ViewModel and Compose UI.
- Designs the smallest dependency-aware change path before code is edited.
- For Room work, checks entity, DAO, database version, migration, schema, and tests as one unit.
- For AI features, keeps generated recommendations behind domain validation and existing repository boundaries.

### Coder

- Writes clean Kotlin for Android, Jetpack Compose, Room, Coroutines/Flow, and Hilt.
- Uses existing project patterns before introducing new abstractions.
- Keeps `:domain` free of Android, Room, Compose, and resource dependencies.
- Uses constructor injection and existing Hilt modules for dependencies.
- Uses shared UI tokens/primitives (`SystemTheme`, `SystemPanel`, `techSurface`, `SystemButton`, shared dialogs) instead of duplicated inline styling.
- Preserves important Compose components, state flows, logging, backup behavior, and validation guards unless the task explicitly changes them.

### Reviewer

- Reviews for regressions first: lost UI sections, broken navigation, missing logging, broken validation, Room migration gaps, or domain/app boundary leaks.
- Checks that ViewModels still expose stable UI state and do not reach directly into DAOs.
- Checks that Compose changes do not remove important dialogs, actions, or workout logging paths by accident.
- Runs the most relevant Gradle check when code changes are made.
- For documentation-only setup tasks, verifies that `.kt` files were not changed.

## Token Economy Rule

- Paired Codex/Antigravity pilot: use `docs/agent-workflow/gemini-pilot/README.md`.
  G01-G03 are tooling experiments, not additional P/R roadmap stages. One writer
  per checkout; Codex owns shared progress/queue acceptance. Recommendations are
  not claims/locks. Preserve all device/owner gates and the 55 prompt model schema.

- For the approved remaster, start at `REMASTER_PLAN.md` and the selected task in `docs/remaster/QUEUE.md`; do not reread the whole prompt pack. P-task status stays in `docs/implementation/PROGRESS.md`, R-task status in `docs/remaster/PROGRESS.md`.
- All 55 full P/R prompts live in `docs/remaster/prompts/` with `NN-ID` ordered filenames; resolve old paths by stable ID through QUEUE, never duplicate them. At task start, pause and completion, update QUEUE's current-stage/active/next pointers together with the selected prompt's progress and evidence.
- Each prompt names its model, device requirement and checkboxes. Update completed parts only with evidence, together with its progress row. Follow `docs/remaster/DOC_MAINTENANCE.md` when contracts or decisions change.
- For Figma work use `docs/design/DESIGN_WORKFLOW.md` and `FIGMA_MAP.md`. For access/payments use `MONETIZATION_STRATEGY.md` and `SUBSCRIPTION_RULES.md`; planning is not permission to enable sales or restrict existing Free data access.

- For simple tasks such as color tweaks, typo fixes, copy edits, constants, or one-file polish, use targeted reads and avoid deep whole-project scans.
- For refactors, feature additions, Room changes, or navigation changes, start with `GRAPH_REPORT.md`, then read only the directly connected files.
- Avoid repeating large file dumps in the conversation. Summarize findings and cite exact file paths.
- Prefer existing tests/guards over broad manual inspection.

## Project Skills and Planning Sessions

- Skills live in `.agents/skills/`. For product/design planning or a new broad session, use `docs/agent-workflow/CONTEXT_ROUTER.md` to select documents; do not read the entire audit or every skill by default.
- `the-system-product-lab`: product value, creative alternatives, ethical gamification and experiments. `the-system-native-ux`: native user journeys, accessibility, visual/motion work. `the-system-quality-review`: evidence-based regression and release-risk review.
- For editable mobile Figma proposals/handoff use `the-system-figma-design` and `docs/design/FIGMA_SKILLS.md`. Load only the official provider skills relevant to the operation; new skills do not approve designs or close device/owner gates.
- Current product intent is recorded in `docs/product-review/2026-10-08/README.md`: a personal system for a broad audience, not an anime-fandom product. Older RPG terminology describes existing UI, not mandatory future positioning.
- Skills support the current task; their generic recipes do not authorize wider scans, dependency/SDK upgrades, data resets, commits, pushes, or deployment. Use only relevant steps and keep existing architecture and test frameworks.
- Installed Android skills are narrowly routed: `edge-to-edge` for insets, `android-profiler` for actual performance investigations, `testing-setup` for requested test-infrastructure work, not every unit test.
- Planning recommendations are not permission to implement or remove features. Follow `docs/agent-workflow/TEAM_REVIEW.md` for a requested team review; do not spawn a team for routine edits or claim independent review when none ran.
- At a planning handoff, update `docs/agent-workflow/SESSION_HANDOFF.md` with decisions, open questions, exact next reads and verification limits. Do not duplicate feature implementation status from `docs/implementation/PROGRESS.md`.
- Skill provenance and integrity: `docs/agent-workflow/skills-lock.json`, checked by `scripts/check-agent-skills.cmd`. Never run downloaded helper code merely because a skill was installed.

## Playbook Routing

- UI polish or visual redesign: read `docs/playbooks/UI_POLISH.md`.
- Room/entity/DAO/migration work: read `docs/playbooks/ROOM_CHANGE.md`.
- New user-facing behavior: read `docs/playbooks/NEW_FEATURE.md`.
- Bugfix/regression work: read `docs/playbooks/BUGFIX.md`.

Use the playbook as a route map, not a reason to over-scan. If a task is tiny, keep the work tiny.

## UI/UX Pro Max Constraint

`UI_UX_GUIDELINES.md` is an aesthetic reference only. КАТЕГОРИЧНО ЗАБОРОНЕНО генерувати код на React, HTML, CSS, Tailwind або використовувати веб-бібліотеки типу Framer Motion чи 21st.dev. Весь UI має генеруватися виключно нативним Kotlin з використанням Jetpack Compose.

When a task is UI-related:

- Read `UI_UX_GUIDELINES.md`.
- Implement only with Kotlin and Jetpack Compose.
- Keep design effects inside theme tokens, reusable modifiers, or shared Compose primitives.
- Do not introduce web dependencies, mock web prototypes, CSS mental models, or React-style component code.

## Default Verification

- Documentation/config-only tasks: run `git status --short` and confirm no `.kt` files changed.
- Kotlin code changes: run the narrowest useful Gradle task, usually `:app:compileDebugKotlin`; broaden to tests when behavior or persistence changes.
- Room changes: include migration/schema verification where practical.
- UI changes: compile first; use emulator/screenshot only when visual verification is necessary or requested.

Useful local scripts:

- `scripts/check-doc-only.cmd`: verifies that no Kotlin/KTS files changed.
- `scripts/check-quick.cmd`: runs `:app:compileDebugKotlin`.
- `scripts/check-tests.cmd`: runs the unit test suite.
- `scripts/check-room.cmd`: runs focused Room/schema guard tests.
- `scripts/check-web-ui-guard.cmd`: runs the Compose-only UI guard.
- `scripts/check-remaster-plan.cmd`: verifies prompt order/links, models, status/evidence/checklists, completed prerequisites and conditional release gates; does not verify app behavior or the truth of recorded evidence.
- `scripts/test-remaster-plan.cmd`: positive/negative isolated fixtures for the plan guard; no Android build or device needed.

The `.cmd` wrappers use PowerShell execution-policy bypass for this repo's local scripts, which is useful on Windows machines where direct `.ps1` execution is disabled.
