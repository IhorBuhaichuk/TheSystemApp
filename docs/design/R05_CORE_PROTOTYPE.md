# R05 / Core Prototype Worklist

Started2026-10-14. Current2026-10-15/D30: v2 visual/content approved; interaction audit
and scoped code/prompt repairs authorized. Prototype gates still open; no R06 or push. Reuse file Gjpbz08rE8dVNvQhHp9vHT, approved Status
188:178/39:683/39:1084. Do not redraw/reopen its titles, icons, W02 or D27/D28 decisions.

## Owner-Requested Pause / Review Agenda

Current2026-10-15: [v2](R05_V2_REVIEW.md) accepted. R05 is `in_progress`, not needs_owner.
Earlier code/prompt hold is superseded by the data-flow request.
[R05_DATA_FLOW](R05_DATA_FLOW.md) owns control-to-domain contracts and task routing.
Secondary/360 prototype links, same-fixture Status data and shared bindings remain open.
The instructions below are historical, not active restrictions.

Previous research-only instruction (now superseded): owner explicitly authorized competitor/scientific research
and proposals in chat, with NO Figma changes. First research pass is recorded in
[R05_RESEARCH_PROPOSALS](R05_RESEARCH_PROPOSALS.md). Findings are not approved requirements;
R05 remains `needs_owner` pending discussion, not ready for final prototype approval.
No code/Figma changes were made during this research. Next: discuss proposals before implementation.

2026-10-14: owner explicitly stops R05 for a broader discussion. Status `needs_owner` means
paused pending questions/product decisions, NOT that the full prototype is ready for approval.
Do not resume Figma edits, research or implementation automatically. Preserve existing specimens.

The requested revision is not merely visual:

- Design: reconsider the new onboarding, logging and report presentation after specific feedback.
- Plain-language copy: explain every question, choice, result and next action without requiring fitness expertise.
- Product logic: review what the app asks, proposes and uses to form a personalized plan.
- Input value: for every proposed onboarding question record why it matters, whether it is required,
  how the answer changes the plan and what happens if information is unavailable. Avoid collecting data merely for completeness.
- Competitors: when authorized, inspect current comparable journeys, inputs and plan/report explanations;
  record dated sources and distinguish observed behavior from inference. Do not copy flows uncritically.
- Scientific basis: when authorized, consult relevant guidelines, systematic reviews and primary evidence;
  record population, applicability, uncertainty and practical limits. Existing rep/goal fixtures are not validated prescriptions.
- Safety and boundaries: discuss suitable defaults, limitations and referral/stop conditions without diagnosis,
  medical guarantees or treating an AI suggestion as the final rule.

The discussion/research pause above is historical and superseded by D30. Follow the
current checklist and resume order below. Accepted visual appearance does not validate
clinical prescriptions; new domain implementation remains in its assigned P/R tasks.

## Goal and Ownership

User goal: create a usable first plan, understand today's action, record actual sets,
finish once and understand the local saved report. A prototype simulates contracts; it
does not execute Room or prove the future P07-P10 implementation already works.

| Control / state | Existing event / outcome | Prototype boundary |
| --- | --- | --- |
| Accepted G/H/S/E/B/P setup and manual alternative | Existing ONB-01..08 is only the old implementation baseline; new state/events defined in R05_DATA_FLOW/P07 | Name optional/later; no permission/account/Pro prerequisite; typed weekly/time/suitability inputs and usable program save are pending P07 |
| Open workout / View plan | STA-01 -> Routes.Cycle, then onOpenMainWorkout | Status does not auto-start or complete a workout |
| Input weight/reps/time | ActiveDayCard -> WorkoutViewModel onSetWeightChanged/onSetRepsChanged/onSetFocusLost | Draft input is not completed; use explicit completion callback |
| Toggle completed / add set | onSetCompletionChanged/onAddSet | Actual selected set identity, new sets incomplete |
| Close / resume draft | Workout dialog dismiss / compatible restore; conflict keep vs explicit discard-and-continue | Close is not discard; no mixing dates/templates |
| Finish / save / failure | loggingSummary.canFinish, onFinishWorkout/isFinishing | Disable repeat submission; failure preserves input/retry |
| Report / dismiss / analysis | WorkoutReport onDismiss/onOpenAnalysis(sessionId) | Saved only after simulated commit; local verdict independent of AI |
| Todo checkbox/menu/hold | STA-06..09, existing identities and row-wide hold about1-1.5s | Native timer/drag/scroll proof belongs R08; state transitions may stand in for free dragging |

Source entry points: R05 prompt + UX_CONTRACT J01-J04/J08 and ACTION_INVENTORY ONB/STA/workout/report rows.
No full-repo rescan. The four Kotlin prompt entry files were inspected at current dirty revision.

## Required State Groups

1. Onboarding: G goal/manual, H recent practice, S weekly days/time, E equipment,
   B suitability/SB unresolved, P editable preview, saving/error/retry/back. Name optional/later.
2. Today: training, rest, recovery, unknown and no-plan; alternate actions follow domain/planned contracts.
3. Cycle/entry: inspect plan, open workout, return to Status; no date mutation from preview.
4. Logging: incomplete input, explicit completed set, long title, keyboard, add/edit path and finish guard.
5. Draft: close/resume compatible, conflict keep/discard confirmation/cancel.
6. Finish: saving, saved partial/full fixture as appropriate, retry without fake saved state.
7. Local report: completed facts/progression or insufficient history/system verdict/next action, AI unavailable useful.
8. Todos: menu/delete confirm or cancel, hold/reorder/cancel while retaining normal taps.

Both360x640 and390x844 core frames; fixed readable type, real editable text/vectors,
Auto Layout for related groups and shared component instances/materials. Preserve the approved main canvas.
Figma browser fallback is available; previously confirmed Starter MCP quota is not retried repeatedly.
No private editor-state access, paid upgrade, sharing change, plugin installation or web prototype.

## Current Checklist / D30

- [x] 2026-10-16 partial execution saved:12 styled normal frames,13 shared action
  instances and8 secondary edges; actual resume/report/details path verified.
  [Checkpoint](../remaster/evidence/2026-10-16-r05-prototype-checkpoint.md),
  [node ledger](assets/r05/closure-state.json). MCP quota/browser plugin failures recorded.
- [x] R04/D29 prerequisite and current source/action boundaries verified.
- [x] v2 visual/content accepted by owner;17 compositions at each size recorded.
- [x] Control/data-flow audit and55-task prompt reconciliation verified; see R05_DATA_FLOW.
- [ ] Complete accepted G/H/S/E/B/P setup, manual alternative and return/error paths.
- [ ] Complete day-type, workout/draft/logging/report and todo state frames.
- [ ] Wire forward/back/cancel/retry routes; inspect actual Present paths at both sizes.
- [ ] Complete shared bindings and consistent plan/Status fixtures without changing D29 layout.
- [ ] Record final verified edges/screenshots/limits in FIGMA_MAP and prepare neutral R06 tasks.
- [ ] Final R05 handoff synchronized; no need to ask again for unchanged v2 visual approval.

## Resume

### Historical first iteration / not the accepted v2 checklist

- [x] Native editable core scenes with vertical Auto Layout bodies and linked System/Button instances.
- [x] Linked System/OutlinedTextField for empty, focused, filled and validation specimens.
- [x] Actual Present walkthrough at360x640: N00 -> N01 -> N02 -> G3 -> E2 -> X0 -> C0
  -> accepted Status39:683 -> PLAN -> W0 -> WK -> WV -> W1 -> WS1 -> RP1 -> Status.
- [x] Equipment overflow scroll reaches its CTA; X0 Back restores equipment and its scroll position.
- [x] Entered values remain incomplete; explicit first checkbox enables finish; saving precedes local report.
- [x] Host checkpoint: plan guard PASS, diff check exit0, all472 Kotlin/KTS hashes unchanged.
- [ ] Complete390x844 reactions and repeat the whole walkthrough. Fourteen editable scenes exist,
  but only the first two reactions have been entered; this is not a second-size PASS.
- [ ] Complete alternate selections/day types, draft/discard/conflict, add/edit/toggle-back, failure,
  report analysis, todo menu/hold/reorder/cancel and all backward routes. Existing specimens are not proof.
- [ ] Finish shared material/text-style bindings and target-size audit for imported choice/set plates.

Fixture: Ihor, Stability, dumbbells + bench, beginner, three-day cycle, Workout A,
five exercises/15 sets. The path finishes ONE set, not an entire workout. The current mapper counts
an exercise once any of its sets is completed (`WorkoutViewModel.kt:1172`), so the report must say
1/15 sets and1/5 exercises; it must not claim zero exercises or15 completed sets.
Choices are currently fixture specimens, not a working arbitrary plan generator. Name/numeric
keyboards simulate preset input; no native IME, free text validation or Room commit is executed.
The report's maintain-load verdict is a synthetic insufficient-history example, not an evaluated workout.

### Resume order

1. Read accepted [v2](R05_V2_REVIEW.md) and [R05_DATA_FLOW](R05_DATA_FLOW.md).
   Visual/content approval is already recorded/D30; do not reopen the direction vote.
2. Use v2 node IDs, not the historical v1 ledger. Complete secondary/360 interactions,
   shared bindings and open state coverage. Align sample plan/Status data without
   changing D29's accepted composition; label simulated inputs honestly.
3. Verify each claimed route at both sizes; record screenshots/edges and remaining
   limitations. Re-approval is needed only for a new product/design change, not to
   repeat the existing approval. Clinical rules still need qualified review.
4. Close R05 only after its prototype gates; then the next queued file is08-R06.
   Native implementation follows the updated NN order, not all at once in R05.

Editor1817311568, page190:210 Phone previews; old1817311492 lost automation attachment.
Present1817311571 contains the first iteration. Inspect tab inventory before reuse.
Three pages already exist; do not buy a plan or delete masters to add another.
R04 overview/page0:1 remains clean. New scenes sit to the right on02 Phone previews.
Native source asset generator: [prototype-scenes.mjs](assets/r05/prototype-scenes.mjs), SVG transport
only, not a web UI. Native clipboard payloads are opaque; do not inspect private Figma state.
Layer virtualization/stale selection caused failed actions: verify selected ID and Present result,
not merely successful clicks. Browser reload needs the actual editor to finish loading.

## Neutral R06 Tasks / Draft

Do not run the participant study during R05. After the full prototype is stable, freeze its revision.
Ask participants: (1) Set up a first plan. (2) Explain what you would do next and why.
(3) Record the result of one approach. (4) End the workout when ready.
(5) Explain what was saved and what you would do next time. (6) Return to your day.
Do not name buttons or tell participants where to tap. Record intervention, confusion and dead ends;
the fixed test dataset/keyboard simulation must be disclosed by the facilitator.
