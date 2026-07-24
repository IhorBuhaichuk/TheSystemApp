# New Feature Playbook

Use for adding a new user-facing feature, screen flow, domain behavior, AI action, or long-lived setting.

## Read First

1. `GRAPH_REPORT.md`
2. Existing feature package most similar to the new work.
3. Relevant domain use cases/models.
4. Relevant repository contracts and implementations.
5. `UI_UX_GUIDELINES.md` if UI is involved.

## Design Path

- Start in `:domain` when the feature adds a business rule, policy, decision, or use case.
- Add or extend repository contracts in `:domain`; implement them in `:app/data`.
- Use Hilt constructor injection and existing modules.
- Expose feature state through a ViewModel; keep Compose screens state-driven.
- Reuse routes in `AppNavGraph.kt` and existing bottom-tab conventions when navigation is involved.

## AI Feature Rule

- AI may suggest, summarize, or analyze, but final plan mutations must pass domain validation.
- Keep provider SDK calls in `:app/data/remote` or `repository_impl`, not in domain.

## Verification

- Run `scripts/check-quick.cmd` after implementation.
- Run targeted tests for affected use cases/ViewModels/repositories.
- Update `GRAPH_REPORT.md` when new architecture edges are added.
