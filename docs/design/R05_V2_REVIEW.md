# R05 v2 / owner review

2026-10-15 / D30: owner explicitly accepts the current v2 mockups and requests a
control-to-data-flow audit, necessary interaction repairs and sequential implementation
prompts. The earlier code/prompt hold is superseded by that request. Visual/content
approval is recorded; prototype wiring, Android implementation and runtime proof
remain separate. R04 Status stays the approved visual baseline.

## Design contract

- Short staged setup: goal, recent practice, realistic schedule/time, equipment,
  suitability/limitations, editable plan preview. Optional personal name is deferred.
- Keep the existing manual-plan path visible. No account, AI or subscription prerequisite.
- Stable plan settings are separate from temporary session changes.
- Fixture: adult general-fitness user, returning after a break, home dumbbells,
  Monday/Wednesday/Friday, approximately35 minutes. All sample prescriptions are
  illustrative and pending qualified review, not personalized clinical advice.
- Workout fixture has4 exercises and8 working sets. Entered input is not completed.
  Partial report records1 completed set, not1 completed exercise or full plan.
- Preserve technique/help, set editing/adding, draft/resume, explicit finish,
  partial confirmation, save-error/retry and saved-report details paths.
- No recovery percentage, punishment for missed days, promised fat-loss rep range,
  mandatory strength test or pretend AI generation.
- Use existing graphite/glass/cyan palette, Roboto, rounded major surfaces and
  top-left edge lighting. Warm amber only for limitations; green for saved/completed.
- Native mobile-sized editable design layers,360x640 and390x844. No HTML/web prototype.

## Review checklist

- [x] Research direction and Figma-only revision authorized by owner.
- [x] Existing Figma file/phone page inspected; MCP Starter limit confirmed once.
- [x] 17 compositions at each size placed as native phone frames, with editable text/vectors.
- [x] Main actions verified; alternate/360 interactions explicitly listed as pending below.
- [x] Compact logger inspected in actual Present; all4 exercise rows and finish CTA visible.
- [x] Node map, evidence and current-stage records updated for the v2 review checkpoint.
- [x] Owner visual/content approval received (D30, 2026-10-15).

## Boundary with implementation

New schedule/time/suitability inputs still need domain/persistence implementation.
The authorized audit and interaction contracts live in [R05_DATA_FLOW](R05_DATA_FLOW.md).
Do not remap calendar-cycle length to weekly frequency. Follow the numbered queue;
visual approval does not mean that the current Android code implements these inputs.

Related: [research](R05_RESEARCH_PROPOSALS.md), [worklist](R05_CORE_PROTOTYPE.md),
[Figma map](FIGMA_MAP.md), [progress](../remaster/PROGRESS.md).

## Native frames / 2026-10-15

File `Gjpbz08rE8dVNvQhHp9vHT`, page `190:210`.
Review sections:390 `223:1022`,360 `225:1872`. Wide sections are review boards,
not phone screens. Every phone frame below is390x844 or360x640 logical units.

| Key | Screen/state | 390 node | 360 node |
| --- | --- | --- | --- |
| G | Main goal / optional manual-plan entry |223:1004|225:1855|
| H | Recent training / return from a break |223:1005|225:1856|
| S | Weekly frequency, weekdays and duration |223:1006|225:1857|
| E | Location and equipment |223:1007|225:1858|
| B | Suitability/limitations |223:1008|225:1859|
| P | First-plan preview |223:1009|225:1860|
| PL | Exercise list |223:1010|225:1861|
| L0 | Entered values, no completed sets |223:1011|225:1862|
| L1 | One explicitly completed set |223:1012|225:1863|
| F | Partial-finish confirmation |223:1013|225:1864|
| R | Saved partial report |223:1014|225:1865|
| D | Resume draft |223:1015|225:1866|
| A | Today-only adjustment |223:1016|225:1867|
| EW | Equipment capacity / unknown |223:1017|225:1868|
| SE | Save failure / preserved draft |223:1018|225:1869|
| SB | Standard-plan suitability unresolved |223:1019|225:1870|
| RD | Saved facts/details |223:1020|225:1871|

## Observed checks and limits

Latest2026-10-16 execution supersedes the earlier all-unbound/secondary-unwired checkpoint:
[saved changes and verification](../remaster/evidence/2026-10-16-r05-prototype-checkpoint.md).
Twelve390 frames styled,13 shared action instances,8 secondary edges; remaining22
frame bindings and further routes/states still open. D29 composition unchanged.

- Actual390 Present walkthrough: G -> H -> S -> E -> B -> P; P exercise link -> PL
  -> L0. Disabled finish stays L0; explicit first checkbox -> L1 -> F; cancel -> L1;
  return to F then save -> R. Report renders1 actual set,8kg x10, no fabricated progress.
- P save and R primary destinations read back as approved `Status - 390x844`.
  Approved Status appearance was not edited. Its older45min/5-exercise fixture differs
  from this new35min/4-exercise example; cross-screen fixture alignment is still open,
  not an implemented plan update or a fully consistent end-to-end app data flow.
- R details destination RD is configured; return from RD and other secondary branches
  remain unwired. No Android persistence, arbitrary input, plan generation or keyboard executes.
- Native G/390 body `223:155` is vertical Auto Layout, gap8, width358, height304/Hug,
  top constraint. Manual entry is separate native frame `231:135`. Roboto text inspected.
- All specimens are editable; full shared component/variable/style bindings are NOT
  complete. Do not describe this review revision as a finished reusable design library.
- Choice alternatives, manual-plan setup, equipment capacities, suitability branches,
  add/edit/toggle-back, technique/menu, draft deletion, save/retry and360 interactions
  require further prototype work after visual/content feedback.48-unit secondary hit
  areas, keyboard/fontScale/Android insets and accessibility are not proven here.
- Native SVG transport source: [review-v2.mjs](assets/r05/review-v2.mjs); generated SVGs
  are editable import material, not the canonical prototype or production Compose.
- Actual rendering evidence: [compact logger](assets/r05/v2-present-360-logger.png),
  [partial report](assets/r05/v2-present-report.png), [review overview](assets/r05/v2-overview-390.png).
- Both review sections retain frame names but redundant34 generated captions were removed.
  Existing v1 scenes and component masters were preserved, not deleted or reused as approval.

Host checks: `check-remaster-plan.cmd` PASS (54 prompts,1132 links before the last
evidence link); `git diff --check` exit0 with existing CRLF warnings. `check-doc-only.cmd`
exit1 reports pre-existing P02/P03 Kotlin/KTS changes, not a clean-tree PASS. No Android
build/device tests or fresh source-hash baseline comparison in this design-only turn.

Review the proposal before finalizing clinical/suitability handling or prescribing a
specific exercise dose. The static selected answers do not validate recommendations.
