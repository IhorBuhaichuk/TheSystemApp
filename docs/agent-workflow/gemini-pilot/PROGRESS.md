# Pilot progress

Date: 2026-10-16. Coordinator: Codex. Sequential, one writer per checkout.
Main remaster stays R05. CLI 1.4.0 installed; Google login required; model access
unverified. Figma configuration/authentication are tracked separately.
No pilot task has executed.

| ID | Recommendation | Execution state | Claim | Actual model / effort | Evidence / next action |
| --- | --- | --- | --- | --- | --- |
| G01 | Gemini 3.8 Flash / Low; fallback GPT-6 Luna / Low | pending | none | unverified | Sign in, verify models, coordinator claim |
| G02 | Gemini 3.8 Flash / Medium; fallback GPT-6.1 Sol / Medium | pending | none | unverified | Requires accepted G01 |
| G03 | Gemini 3.8 Flash / Medium; fallback GPT-6.1 Sol / Medium | pending | none | unverified | Requires accepted G02; test-only |

States: pending -> claimed -> in_progress -> review_ready -> accepted;
changes_requested or blocked_auth/tool when appropriate. Only Codex updates this
registry. Claims are advisory, not atomic filesystem locks. Release on interruption.
Existing P/R checkboxes and status meanings remain unchanged.

- [x] Official installer inspected; CLI version/help verified.
- [x] Bounded pilot briefs and shared entry rules prepared.
- [ ] Personal Google login and model entitlement verified.
- [ ] Figma endpoint configuration/authenticated read-only tools verified.
- [ ] G01 accepted with evidence.
- [ ] G02 accepted with evidence.
- [ ] G03 accepted with tests and diff review.
- [ ] Adoption decision recorded using correction/review cost.
