---
name: the-system-quality-review
description: "Review THE SYSTEM changes or proposals for regressions, data safety, evidence gaps and release risk. Use for focused QA/code/product-risk review, not automatic whole-repo audits."
---

# Quality review

## Scope and evidence

Follow repository AGENTS.md and [CONTEXT_ROUTER](../../../docs/agent-workflow/CONTEXT_ROUTER.md).
Read GRAPH_REPORT, the relevant FEATURE_MAP row and playbook, then the target
diff/contracts/tests. Check revision and working changes before repeating an old
audit finding. Implementation progress and evidence, not a plan, establish closure.

Review only; do not fix code unless asked. Preserve unrelated local changes.
Distinguish product hypotheses, static code findings, tests run now, historical
evidence and unavailable runtime checks. Never claim independence if the same
agent merely changed roles; use [TEAM_REVIEW](../../../docs/agent-workflow/TEAM_REVIEW.md)
for a requested independent pass.

## Risk-directed checks

Choose relevant items, not a mandatory exhaustive scan:

- First launch: deterministic route, saved onboarding state, useful first action, no route flash; distinguish initialization from a usable plan.
- Logging: all existing input/action paths remain reachable, incomplete sets are clear, draft survives relevant lifecycle boundaries, finish cannot double-write XP/history.
- Data: targeted history edits, transaction boundaries, schema/migration parity, backup preview/validation/confirmation and non-destructive failure. No real users yet does not make developer data disposable.
- Decisions/AI: unknown readiness is not certainty, rest/recovery are not overridden by engagement incentives, useful local fallback, no raw errors, ValidateDirectivesUseCase remains the final gatekeeper.
- UI: no lost content/actions, clipped critical text, hidden CTA or stolen scroll; tokens stay shared and UI stays native.
- Privacy/release: data minimization, honest permissions/docs, no keys or sensitive traces in commits; distinguish fitness guidance from unsupported medical claims.
- Performance: same device/build conditions and before/after evidence. A new library or a baseline profile is not evidence of improvement by itself.

## Verification and output

Select the narrowest relevant existing check. Broaden for shared behavior, Room
or release risk. `testing-setup` is for requested infrastructure work, not adding
a test to an existing suite. No SDK/test-stack upgrade simply to satisfy a recipe.
Do not clear app data or install over a device without scope and authorization.

Present findings first, ordered by severity: file/symbol, observable risk,
evidence, smallest remedy and exact verification. Then list assumptions,
unverified gates and what is already sound. Use P0 only for a genuinely blocking
issue; do not inflate every improvement into a release blocker.

If no issues are found, say so and name residual risks/test gaps. Do not assign
measured quality, retention, demand or performance scores without measurements.
For docs-only work in a dirty tree, compare source hashes against task start;
report pre-existing check-doc-only failures honestly rather than resetting files.
