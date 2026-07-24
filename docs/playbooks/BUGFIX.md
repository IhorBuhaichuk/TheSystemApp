# Bugfix Playbook

Use for crashes, incorrect data, broken UI state, failing tests, regressions, and suspicious behavior.

## First Move

- Reproduce from logs, tests, or exact code path before changing code.
- Read `GRAPH_REPORT.md` if the bug crosses feature/domain/data boundaries.
- For one-file obvious bugs, read only the target file and its immediate caller/callee.

## Debug Path

- UI symptom: inspect Screen -> ViewModel -> UI state mapper -> use case.
- Wrong business result: inspect use case and domain model/policy tests.
- Wrong persisted result: inspect repository implementation -> DAO -> entity/migration.
- Navigation issue: inspect `Routes.kt` and `AppNavGraph.kt`.
- AI issue: inspect parser/classifier/repository and domain validation.

## Fix Rules

- Prefer the smallest behavior-correct change.
- Add a regression test when the bug is business logic, parsing, persistence, or a repeated failure mode.
- Do not clean up unrelated code while fixing the bug.
- Preserve logging and guard tests.

## Verification

- Run the narrowest relevant test first.
- Run `scripts/check-quick.cmd` before handing back code changes.
