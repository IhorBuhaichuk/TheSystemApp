# R04 B / Review Round 2

Date: 2026-10-13. Owner feedback is the source of this round.
Historical round state was in_progress. D29/2026-10-14 closes R04 for the accepted initial Status;
no Kotlin edits, R05 start, push or merge. Read [current acceptance](R04_SELECTED_DESIGN.md#final-acceptance--d29)
instead of reactivating the historical candidate gates below.
Historical candidate-generation round. Current owner selections/application are in
[R04_SELECTED_DESIGN](R04_SELECTED_DESIGN.md),2026-10-14/D26. All10 chosen icons/W02
are applied; candidate boards44:768/44:1499 were removed after source/evidence preservation.
The instructions below record earlier work, not a request to regenerate options or ask again.

## Latest Owner Override / Exercise Icons Revision 3

2026-10-13: owner rejects **all five EX01..EX05**, including formerly retained EX01.
Everything else is acceptable as candidates; owner will send final choices later.
Replace these five slots only. Preserve the other45 native icons, all week options,
both mobile frames, labels, CTA placement, navigation and pending progress disposition.
Keep stable EX01..EX05 IDs; no automatic insertion into the workout summary or Kotlin.

- [x] Exact five-slot scope recorded; previous retain-EX01 instruction superseded.
- [x] Five distinct new movement/exercise-set vectors created, not another equipment set.
- [x] Only five native slots replaced; other45 native IDs/names preserved.
- [x] Actual export and focused Figma screenshot inspected.
- [x] Current pointers/evidence/choice ledger synchronized; source hashes and doc checks recorded.

The round2 matrix and journal below are historical evidence for revision2, not a request
to regenerate the other icons again. Final selections remain open.

New candidates: EX01 exercise cards with a movement diagram; EX02 joint range of motion;
EX03 repetition loop; EX04 shoe and exercise mat; EX05 muscle fibers/activation.
Source: [original vector drawings](assets/r04-b/exercise-icons-r3-source.svg),
[structured geometry](assets/r04-b/exercise-icons-r3.json),
[before inventory](assets/r04-b/exercise-r3-native-before.json),
[replacement journal](assets/r04-b/exercise-r3-native-replacements.json).
Native EX01..05 nodes:163:154,158:154,159:154,160:154,161:154. All preserve the same
88x101 slot dimensions, positions, visible IDs and neutral/cyan preview treatment.
[Verification](assets/r04-b/exercise-r3-native-verification.json):50 current slots,
45 kept IDs/names,5 replacements, zero missing. [Focused actual screenshot](../remaster/evidence/screenshots/r04-b/EX-revision3-context.jpg)
and [clean five-option preview](../remaster/evidence/screenshots/r04-b/EX-revision3-context-clean.jpg)
and the updated [full native export](assets/r04-b/icons-50.png) inspected. No candidate inserted
into the mobile screen. Kotlin/KTS baseline:472 paths/hashes unchanged.
Host checks: check-remaster-plan PASS54; git diff --check exit0 with existing CRLF warnings only.
Existing P02/P03 dirty sources remain; no clean-tree doc-only pass is claimed. This narrow change
is complete; R04 still awaits final owner choices and its remaining material/header gates.

## Keep vs Replace

Keep means preserve the candidate, NOT final selection or permission to insert it.
Calendar icons were not reviewed in this message; leave all five unchanged.
Owner's STATUS04 means NAV-STATUS04, not a new ID.

| Group | Preserve unchanged | Replace in the same slots |
| --- | --- | --- |
| Time | TIME04 | TIME01,TIME02,TIME03,TIME05 |
| Exercises | EX01 | EX02,EX03,EX04,EX05 |
| Experience | XP04 | XP01,XP02,XP03,XP05; use readable game-experience cues, not another medal/star/bolt |
| Tasks | TASK02 | TASK01,TASK03,TASK04,TASK05 |
| Completion | CHECK01,CHECK02,CHECK03,CHECK04 | CHECK05 |
| Status | NAV-STATUS04 | NAV-STATUS01,NAV-STATUS02,NAV-STATUS03,NAV-STATUS05 |
| Calendar | NAV-CALENDAR01..05, no new owner choice | none |
| System | none | NAV-SYSTEM01..05; training-plan/development meaning, no gear/hub/tuning tools |
| Statistics | NAV-STATS01,NAV-STATS05 | NAV-STATS02,NAV-STATS03,NAV-STATS04 |
| Profile | NAV-PROFILE01,NAV-PROFILE04 | NAV-PROFILE02,NAV-PROFILE03,NAV-PROFILE05 |
| Week | W01,W05 | W02,W03,W04; no copies of old strip/square/minimal-date options |

Totals: **18 preserved + 32 new icon candidates**, still50 options;2 preserved +3 new week options.
Native retained nodes must not be deleted/rebuilt. Current-board slot IDs stay readable;
record revision2 replacements so old screenshots are not treated as current choices.

## Work Checklist

- [x] Owner retain/replace list recorded, aliases normalized, counts checked.
- [x] CycleScreen and Cycle action inventory inspected; no business logic edited.
- [x] Create32 distinct replacement vectors, replacing only rejected native slots.
- [x] Create3 distinct week replacements, retaining actual W01/W05 nodes.
- [x] Inspect/export all50 icons and all5 week options; no clipping or accidental identical variants.
- [x] Move Open workout below View plan in both B sizes; keep its current action meaning.
- [x] Remove former fixed CTA dock, shift later content/reserve only as needed, preserve all sections.
- [x] Bottom nav is visible and fixed at the bottom in actual Present for both sizes; verify scroll.
- [x] Sync node map, round evidence, active queue and source/doc checks.

## Present / Placement Contract

Owner requested Open workout directly below View plan, as scrolling content, not a bottom-fixed CTA.
Bottom navigation alone stays fixed at the bottom. Preserve both current Cycle destinations:
Open workout does not directly commit/start a session. Preserve action IDs and all downstream data.
Do not remove progress details while moving content. Restrictive recovery/deload variants still need
their relevant warning before an actual start in R05/R08; this training/missing-input fixture does
not authorize hiding safety states. No date mutation or prototype transaction is invented.

## Training Tab / Source Findings and Approved Name

Sources: app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt;
docs/design/ACTION_INVENTORY.md, CYC-01..08; docs/architecture/FEATURE_MAP.md Cycle row.
Audited HEAD36d6c480da90438c1b5adaf363de7cfb6986f9d8, with existing local P02/P03 changes.

Actual contents: cycle/day selection, edit day/cycle, add exercises, start/resume workout,
muscle-group parameters, progression/bonuses, local/AI analysis entry. It is not a general settings tab.
Recommendation: **Тренування**. Alternative **Мій план** emphasizes plan editing but under-describes
execution. **Розвиток** is broader/vaguer and overlaps Statistics. Do not rename route IDs or Kotlin.
Owner selected **Тренування** in the naming question on 2026-10-13. Update both shared Figma
navigation masters; retain NAV-SYSTEM candidate IDs for choice continuity and the internal Cycle
route. This approves the label, not a final icon or Kotlin implementation. Candidates emphasize
training/plan/progression, not general settings tools.

## Progress Details / Proposal Only

Owner explicitly requested advice before changes. Keep existing STA-03 block untouched in this round.
Recommendation: full level/rank/XP overview in Profile; real workout progress/streak in Statistics.
Do not put global level/rank into a specific Planned workout card: different data ownership/meaning,
extra visual noise, and misleading association with that workout. A compact streak on the daily
screen could be discussed separately, but no hidden/removed block or automatic decision is approved.

## Resume / Evidence

Existing native icon board44:768; week board44:1499; B frames39:683/39:1084.
Shared nav masters: Nav36044:705 inside B small, Nav39044:747 at page root.
Before cleanup preserve masters and actual retained candidate nodes. MCP quota is known exhausted;
use browser editing, not repeated blocked calls or paid upgrades. Inspect live UI before every mutation.
Record verified node replacements/Present screenshots here; no runtime claim from a static export.

### Native Candidate Revision 2

- Icon replacements: [original SVG drawings](assets/r04-b/icons-r2-source.svg),
  [structured geometry](assets/r04-b/icons-r2.json), [32 native replacements](assets/r04-b/round2-native-replacements.json).
  TASK01 was refined once more to a pinned note, avoiding the old clipboard silhouette.
  [50-icon native export](assets/r04-b/icons-50.png) inspected; all labels and state pairs render.
- Weeks: [fixture/spec](assets/r04-b/week-r2.json), [native nodes](assets/r04-b/week-r2-native.json).
  W02 tickets130:136, W03 weekdays/weekend130:180, W04 selected-day focus130:206;
  W01 remains44:1505 and W05 remains44:1664. [Native PNG](assets/r04-b/week-options.png) inspected.
  No candidate has been automatically inserted into the selected B screen.
- [Native-ID verification](assets/r04-b/round2-native-verification.json):50 slots observed across
  the virtualized Layers list;18 kept original IDs/names and32 match their replacement journal,
  zero missing or unexpected candidates. Calendar5 untouched. Keep still does not mean final selection.
- Final icon export includes the approved heading `Навігація · Тренування`; stable NAV-SYSTEM IDs
  stay unchanged for selection continuity. New32 original drawings are project assets; only retained
  Google vectors use the existing Apache-2.0 source manifest/license.

### Placement Checkpoint

- Small CTA now135:137 at X20/Y400/W320/H56, scrolling root child. Normal CTA remains39:1194,
  X20/Y400/W350/H56; old dock was ungrouped and its background39:1193 removed. Small former
  background135:136 was also removed. Both CTA paint/effect/text styles retained.
- Load explanation Y400->468; todos462->530; existing progress790->858; reserve1022->1090
  in both sizes. Progress content/action is retained, not inserted into WorkoutSummary.
- Nav360 master44:705 is preserved at page X8000/Y5160; native instance144:156 is now a
  B360 root child at X12/Y552/W336/H88, Fixed. Nav390 instance48:236 remains at X12/Y756.
- Present still hid nav on the first normal check. Inspection found **Fixed (stay in place)**
  on the internal FixedNavigation group of the master as well as on the screen instance.
  Internal groups changed to Scroll with parent; only screen instances are Fixed.
  New captures below, not the first failed check, establish actual Present verification.

### Normal Present Verification

- Internal nav groups39:1065/39:797 now Scroll with parent; screen instances alone remain Fixed.
- Figma viewer was using Actual size100% with a683px browser viewport, clipping the844px frame.
  Options -> Fit width and height fixes the review scale, without changing design dimensions/fonts.
- Normal flow `B · 390x844` has starting point39:1084. [Top](../remaster/evidence/screenshots/r04-b/B-normal-round2-present-top.jpg)
  and [after real vertical scroll](../remaster/evidence/screenshots/r04-b/B-normal-round2-present-bottom.jpg):
  nav visible at the same bottom location, CTA moves with content, all4 todo rows and progress reached.
  This verifies Figma placement/scroll only, not navigation callbacks, Android insets or accessibility.

### Small Present Verification

- Small flow `B · 360x640` starts39:683. Both explicit flows avoid presenting an old rejected frame.
- [Top](../remaster/evidence/screenshots/r04-b/B-small-round2-present-top.jpg) and
  [after real vertical scroll](../remaster/evidence/screenshots/r04-b/B-small-round2-present-bottom.jpg):
  View plan then Open workout above the fixed nav; CTA scrolls away, all4 rows and progress reached,
  nav stays at the same bottom location. No todo row, menu, checkbox or progress action removed.
- Both shared navigation masters now show `Тренування`, propagated into both screen instances.
  Final icon selection is still open; the old temporary nav glyph was not replaced by an arbitrary choice.
- Source baseline comparison:472 Kotlin/KTS paths and SHA-256 unchanged; existing dirty P02/P03
  sources preserved. check-remaster-plan PASS54, git diff --check exit0 (existing CRLF warnings only).
  Current map, prompt/progress/evidence, D25, QUEUE and handoff synchronized. Doc-only is not a
  clean-tree pass because pre-existing P02/P03 Kotlin changes remain; no Android build/test was run.

Round2 work is complete; **R04 is not complete** until the remaining owner choices and material/
header QA gates close. Next: owner chooses candidates by existing IDs, separately approves the
progress-details disposition, then apply chosen assets/copy and run final material/alpha review.
