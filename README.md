# THE SYSTEM: LEVEL UP

THE SYSTEM: LEVEL UP is a native Android fitness planning app built with Kotlin, Jetpack Compose, Room, Hilt, Coroutines/Flow, Health Connect, and optional Gemini-backed AI architecture tools.

The product goal is not just workout tracking. It is a daily decision engine:

> What should I do today, why this, and how does it move me one level up?

## Project Shape

- `:domain`: pure Kotlin models, repository contracts, use cases, validation, and decision rules.
- `:app`: Android shell, Compose UI, Room database, repository implementations, Health Connect, Hilt modules, workers, and navigation.
- Main tabs: Status, Calendar, System/Cycle, Statistics, Profile.
- First-launch flow: Onboarding -> Status / Today Order.

Read `GRAPH_REPORT.md` before refactoring or adding features.

## Release Behavior

- Public release builds disable Gemini client AI.
- Core workout planning, logging, Today Order, statistics, calendar, and fallback reports work locally.
- Health Connect is optional and permission-gated.
- Sensitive Room database files are excluded from Android automatic cloud backup/device transfer.
- Users can create explicit JSON backups from inside the app.

## Important Docs

- `GRAPH_REPORT.md`: architecture map.
- `AGENTS.md`: project rules for AI agents.
- `UI_UX_GUIDELINES.md`: native Compose aesthetic guide.
- `PRODUCT_STRATEGY.md`: product positioning and strategy.
- `MVP_DEFINITION.md`: MVP and release criteria.
- `PRIVACY_POLICY.md`: privacy and data handling.
- `STORE_LISTING.md`: Play Store draft.
- `docs/HEALTH_CONNECT_RATIONALE.md`: Health Connect permission rationale.
- `docs/SCREENSHOTS_CHECKLIST.md`: screenshot and store asset QA.

## Verification Scripts

- `scripts/check-quick.cmd`: compile debug Kotlin.
- `scripts/check-tests.cmd`: run unit tests.
- `scripts/check-room.cmd`: run Room/schema checks.
- `scripts/check-doc-only.cmd`: verify documentation-only changes.
- `scripts/check-web-ui-guard.cmd`: guard against web UI code in this native Android project.

## Safety Scope

THE SYSTEM: LEVEL UP is a fitness planning and self-tracking tool. It does not diagnose, treat, cure, or prevent disease and does not replace professional medical advice.

