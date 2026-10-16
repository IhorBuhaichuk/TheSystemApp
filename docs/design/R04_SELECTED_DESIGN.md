# R04 / Selected Status Design

Date: 2026-10-14. Current entry for applying the owner's explicit choices.
Supersedes candidate-generation instructions in R04_B_REVIEW_ROUND_2, not historical evidence.
Figma only; no Kotlin edits, R05 start, commit, push or merge.

## Final Acceptance / D29

2026-10-14: owner explicitly says the current initial Status screen is fully satisfactory.
**R04 done: approved design baseline, not implemented Android UI or a completed prototype.**
File Gjpbz08rE8dVNvQhHp9vHT: overview188:178/390x946; phones39:683/360x640 and39:1084/390x844.
The approval covers B after D26-D28: current layout, selected icons/W02, button treatment,
«Статус», «Справи», recommendation copy, no progress card/weekly counter and normal bottom gap.
Earlier title alternatives/H01-H05 artwork are not part of this accepted revision. Keep the
current header without inserting an unchosen image; an artwork alpha-mask check is N/A for this baseline.
Historical artwork alpha limits remain documented, not falsely passed or silently fixed.

Acceptance review: original R04 concept/selection scope, shared-library preservation, editable
mobile frames/full overview, actual PNG/Present evidence and STA-01..13 event-disposition map checked.
Owner appearance approval is explicit, not inferred from the earlier placement-only D24.
No unresolved blocker found **within the accepted R04 design-selection scope**.

Not R04 completion claims: R05 interactive journey/alternate states, R06 other screens and
Statistics destination, R07 token/material implementation parity, R08 actual event/gesture/inset
behavior, P19 accessibility/fontScale/contrast. Those tasks retain their tests and approvals.
Current source palettes/shared materials were reused; remaining raw decorative-color bindings
and Android renderer equivalence are implementation verification items, not a reason to redraw this approved screen.
Next eligible design task: R05, pending a separate owner command. Do not start it automatically.

## Approved Selection / D26

| Use | Stable candidate | Source revision |
| --- | --- | --- |
| Duration | TIME04 | Google watch, retained |
| Exercises | EX03 | Exercise revision3, repetition loop |
| Experience | XP01 | Revision2 hexagon with editable XP lettering |
| Tasks heading | TASK03 | Revision2 task stack |
| Completion | CHECK02 | Circle, empty/checked states |
| Status navigation | NAV-STATUS05 | Revision2 direction signs |
| Calendar navigation | NAV-CALENDAR01 | Google calendar_month |
| Training navigation | NAV-SYSTEM01 | Revision2 growth steps |
| Statistics navigation | NAV-STATS05 | Google query_stats, retained |
| Profile navigation | NAV-PROFILE04 | Google manage_accounts, retained |
| Week | W02 | Revision2 date tickets |

XP metadata must be **75 + the XP icon**, no redundant XP text and no added plus sign.
Keep Status/title/date, todos, overflow actions and existing route meanings.
Progress block is explicitly removed from Status by the later D27 decision below.
Current titles/header appearance are accepted by D29; do not substitute older alternatives.

## Weekly Counter / D28

Owner decision2026-10-14: remove `1 / 3 виконаних тренувань` from all3 Status Figma frames.
W02 date tickets, weekday/date labels and completed/planned/current-day cues remain unchanged.
Do not delete weekly aggregation/storage or add a replacement caption. Native Status follows this in R08.

- [x] Removal decision recorded; overrides D26's visible-counter requirement only.
- [x] Remove all3 native text nodes; inspect current export; update current pointers/evidence.

Applied:188:264 and183:169 removed; normal week177:154 replaced by192:102 from the same
W02 source without WeeklyCompletedCount. A transient multi-selection removed its7 dates too;
the exact seven-date geometry/state fixture was restored before acceptance. Normal192:102 remains
362x106 atX14/Y96, with editable day groups192:103/109/115/121/127/133/139. Main/small date IDs
stay unchanged. No date/ticket repositioning, font change, new caption or weekly-metric deletion.

Current evidence: [native full PNG](assets/r04-b/status-minimal-full.png),
[editor](assets/r04-b/status-minimal-editor.jpg),
[360 Present top](assets/r04-b/status-minimal-360-top.jpg),
[390 Present top](assets/r04-b/status-minimal-390-top.jpg).
Inspected: seven dates12..18, selected13/completed12/planned16, TIME04/EX03/XP01/75 retained;
no weekly counter or progress box. D27 scroll-end evidence below remains applicable to the unchanged footer.

## Canvas vs Phone

- Full-content Status overview on the main page, phone-width390, all sections visible on canvas.
- Preserve real360x640 and390x844 scroll viewports for Present/device-size design checks separately.
- Do not shrink fonts or cram the full page into a640px viewport.
- Full overview is a review of scroll content, not a claim that all content fits on a phone at once.
- Retain single-source navigation/material components. A long overview does not authorize Android layout changes.

## Progress Placement / D27

Owner decision2026-10-14: **remove the entire «Деталі прогресу» box from Status**, including
the main full-content Figma overview and both phone previews. No replacement/empty spacer box.
Its information (the current fixture shows streak5 days, level12, rankC) belongs in **Statistics**,
not in the workout card or the Status footer. R06 designs the Statistics presentation; R08 omits
the Status box, R12 exposes its useful information through existing truthful state. Do not delete
stored progress, calculations, history or other existing Statistics/Profile access.

Remove the surplus bottom gap: full overview navigation should follow the last todo with a
normal section gap, and phone scroll content should end with only the reserve needed to clear
the fixed nav. Keep phone heights, nav height/bottom anchor and readable fonts unchanged.
This is an approved Figma layout correction, not permission to edit Kotlin or start R06/R08/R12.

- [x] Decision/destination recorded in the persistent ledger and current entry.
- [x] Remove the box from all3 Figma frames and close excess lower space.
- [x] Verify full export and phone scroll-end states; synchronize handoff/prompts/evidence.

Applied: removed188:258 (overview),39:1187 (390 phone),39:786 (360 phone).
Overview188:178 is now390x946; nav188:313 atY858,24px below the last todo endingY834.
Phone dimensions/nav anchors unchanged. Both1x1 scroll markers44:698/72:136 moved fromY1090
toY945, making content end946: only88px nav clearance plus24px gap after the last todo.
No filler/decorative placeholder or replacement progress card was added.

Before D28 caption removal: [D27 native full PNG](assets/r04-b/status-no-progress-full.png),
[actual editor](assets/r04-b/status-no-progress-editor.jpg),
[360 scroll end](assets/r04-b/status-no-progress-360-bottom.jpg),
[390 scroll end](assets/r04-b/status-no-progress-390-bottom.jpg).
All inspected: no progress card, all4 todos retained,24px end gap, fixed nav visible.
Figma evidence only; Statistics presentation/native implementation is still future R06/R12 work.

## Cleanup Scope

Owner authorizes removing unused alternatives and auxiliary review objects, not necessary masters.
Live selection inspected:12 roots, including rejected B-v1(2), R03 mobile specimen, R03 library sheet,
and8 System component roots. Delete the4 demonstration/historical roots; preserve the8 component roots.
After selected assets are applied, remove icon/week candidate boards, retaining source/evidence locally.
Separate active overview, phone previews and library pages so technical assets do not clutter Status.
No plan upgrade, account/sharing change, history purge, source/data reset or loss of component dependencies.

## Checklist

- [x] Owner choices and safe cleanup boundary recorded.
- [x] Apply metadata icons/75, task heading icon and CHECK02 states in both phone frames.
- [x] Apply all5 nav icons through both shared masters.
- [x] Apply selected W02 week in both phone frames, with existing dates/states; counter removed by D28.
- [x] Create editable full-content overview; retain phone Present checks.
- [x] Remove obsolete alternatives/demos; keep and organize necessary components.
- [x] Inspect native exports and Present; record nodes, preserved actions and gaps.
- [x] Synchronize current pointers/progress/evidence; run doc/source-preservation checks.

## Resume

File Gjpbz08rE8dVNvQhHp9vHT. Main page0:1 contains only the full overview188:178.
Phone previews page190:210 contains B36039:683 and B39039:1084, at0/0 and500/0 respectively.
Components page190:211 contains8 System roots and2 nav masters44:705/44:747.
Phone instances144:156/48:236 remain linked. Candidate boards44:768/44:1499 were removed.
MCP quota remains blocked on2026-10-14; use authenticated browser, no further quota retries/upgrade.
Read only the relevant current screen/group and selected source assets before editing.

### Historical D26 Checkpoint / Before D27

Metadata/title/checkbox work is recorded in [native change journal](assets/r04-b/applied-selection-nodes.json).
Both XP text nodes now75; TIME04/EX03/XP01 are editable vector/text layers beside existing captions.
Both task headings have TASK03; all8 checkbox artworks use CHECK02 with existing2-done/2-open fixture.
Todos, their text/menus, plus actions and progress blocks are preserved.
W02 widths332/362, height106 including counter; fixed fonts10/19, selected13 and dates12..18 unchanged.
Both preserve the1/3 counter and fit before the workout card atY208.
Both nav masters now use all5 selected glyphs; screen instances retain linkage and labels.
Full overview188:178 created from B390,390x1034. Root children set Top before resize to avoid
scaling layout/fonts; nav188:313 moved toY946; overview-only reserve188:307 removed.
Overview has No scrolling/no new flow starting point. Actual phone frames retain original sizes.
Overview mirrors the390px phone content, not a separate design direction; future content/detail
edits must keep both phone previews and the full overview consistent. Shared nav/surface masters remain reused.
Pages and cleanup verified through actual layer inventories:1 main root,2 phones,10 library roots.
Removed6 obsolete roots: boards44:768/44:1499, rejected B-v1 frames27:100/27:291,
R03 specimen12:43 and review sheet3:117. Source files and historical evidence stay local;
library masters, style/variable definitions and production actions were not removed.

## Historical D26 Visual Verification

- [Full native PNG](assets/r04-b/status-selected-full.png),390x1034: all sections visible,
  chosen icons/date tickets present,75 without duplicated XP,4 todos and progress block retained.
- [Actual editor capture](assets/r04-b/status-selected-editor.jpg): one clean main-page frame;
  full height is a canvas review, not a640px phone viewport or a smaller-font layout.
- Present at Fit width and height: [360 top](assets/r04-b/status-selected-360-top.jpg),
  [360 bottom](assets/r04-b/status-selected-360-bottom.jpg),
  [390 top](assets/r04-b/status-selected-390-top.jpg),
  [390 bottom](assets/r04-b/status-selected-390-bottom.jpg).
  CTA is below View plan and scrolls; nav stays fixed; all4 todos/progress are reachable.
- The initial small-size capture exposed the old reward caption. Native text39:733 was
  corrected to75, auto-width14 atX306/Y292; restarted Present capture verifies the correction.
- These are Figma checks, not Android/fontScale/TalkBack or callback/drag tests.
  No R05 interaction wiring or production code change is claimed.

## R04 Closure

No remaining R04 owner choice: D29 approves the current titles, copy, appearance and header as shown.
Unchosen header concepts are outside the accepted baseline, not pending work to insert automatically.
Native binding/material/contrast/accessibility verification remains R07/R08/P19; R04 does not mark it passed.
No need to choose the10 icons/W02 again. R05 is next but has not started.

## Closure Verification / D29

- check-remaster-plan: PASS54 prompts/1095 local links, completed R04 prerequisites/checklists and next pointers.
- git diff --check: exit0; existing CRLF notices only.
- Source SHA-256 at closure-review start vs end:472/472 Kotlin/KTS paths identical, zero changed/missing.
- check-doc-only: exit1 from pre-existing P02/P03 Kotlin/KTS changes; source preservation is confirmed
  by per-review hashes, not a false clean-tree pass. No reset/revert, Kotlin edit or Gradle/device test.
- Current native full PNG and both saved Present tops reread; no new Figma edits or claim of rerunning
  native/connected tests. Final acceptance is owner's explicit D29 statement.
- R04 done, next eligible R05 not started. No commit/push/merge or production scope change.

## Historical D27-D28 Host Checks

- check-remaster-plan: PASS54 prompts/1090 local links; current pointers/checklists consistent.
- git diff --check: exit0, existing CRLF notices only.
- SHA-256 baseline vs final: all472 Kotlin/KTS files identical, zero changed/missing.
- Existing P02/P03 dirty Kotlin is preserved; no clean-tree doc-only pass claimed.
- Full native PNG, both Present tops and D27 scroll ends inspected. No Android build/device,
  runtime interaction claim, R05 start, commit/push/merge. R04 remaining owner/material gates stay open.

## Historical D26 Host Checks

- check-remaster-plan: PASS54 prompts/1064 local links, current pointers and dependency checks.
- git diff --check: exit0; existing CRLF notices only.
- SHA-256 before/after: all472 Kotlin/KTS paths identical, zero changed/missing.
- check-doc-only: exit1 from pre-existing P02/P03 Kotlin changes; not claimed as a clean-tree pass.
- No Android build/device test needed for this Figma/docs-only request; no commit/push/merge.
