# R04 Status Directions

Date: 2026-10-13. Revision: R04-v1. Status: **rejected by owner; historical only**.
Owner rated this revision1/10: alternatives were too similar, unclear labels, unstructured week/todos,
oversized AddTask and misaligned navigation labels. Recommendation B is withdrawn.
Current requirements: [R04_REWORK_CONTRACT](R04_REWORK_CONTRACT.md). No v1 node is approved.
Six editable native frames in the existing owner draft; no production source change.
R03 [foundations](R03_FOUNDATIONS.md) and R02 [contracts](UX_CONTRACT.md) remain authoritative.
This is a comparison of presentation proposals, not three shipped themes or approval of H01-H06.

## Actual Frames

| Direction | Small 360x640 | Normal 390x844 |
| --- | --- | --- |
| A / Restrained Tech | [19:116](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=19-116) | [27:230](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=27-230) |
| B / Practical Training | [27:100](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=27-100) | [27:291](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=27-291) |
| C / Readable HUD | [27:161](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=27-161) | [27:352](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=27-352) |

All names start R04 A/B/C and end proposed. Page remains 01 Foundations (0:1), separate from
R03 component masters and its specimen frames. Native text and linked Surface/Button/ProgressBar
instances were reused; navigation glyphs are editable SVG-derived vectors, not screenshot layers.
No new theme, variable mode, paid upgrade, Code Connect or installed plugin.

## Shared Synthetic Fixture

- Status; Tuesday 13 October, user Ihor. This is synthetic example data, not developer fitness records.
- Training A, 45 minutes, 5 exercises, +75 XP; reason: scheduled strength session.
- Readiness unknown: sleep not connected; consider how you feel; not a medical assessment.
- Primary action: open workout via Cycle; secondary: view plan. Neither means auto-start or commit.
- Week 2/3; identical seven-day string; streak 5; quests 2/4; today 420 XP, multiplier x1.2;
  level12, 620/1000 XP (62% bar).
- Four identical todos, two checked: mobility10m and planning; reading20m and meditation10m open.
  Add task action visible below the list; no synthetic claim that a todo is workout proof.
- Profile/details: rankC, level12, strength42, endurance38, discipline55, intellect41, strategy37.
- Same five labels and order: Status, Calendar, System, Statistics, Profile (Ukrainian in frames).

All full information is in vertically scrolling content. Fixed navigation is one opaque,
background-token-bound 88-unit wrapper with the existing rounded72-unit Surface and editable
glyphs/labels. The wrapper masks scrolling text; bottom padding108 reserves scroll clearance.
Small uses20 horizontal margin. Normal uses a bounded320-wide column and35 margin; this is an
explicit concept fixture, NOT proof of arbitrary responsive widths or Android insets.

## Differences and Trade-offs

| Direction | Proposal | Benefit | Cost |
| --- | --- | --- | --- |
| A | Compact16/20 task title;26/28 screen heading; plan and readiness before action; restrained material | Quiet, close to current foundations; explanation precedes execution | Smaller12/18 supporting copy; action lower in first viewport |
| B | Task title26/28, body14/20,144-high task panel; action before readiness; readiness Normal surface | Fast task recognition, clearest training-first sequence; primary action higher | Readiness explanation lower; must stay explicit for safety-critical future states |
| C | Screen heading34/36; task26/28, body14/20,128-high Active surface; explanation before action, no redundant action section label | More recognizable HUD through hierarchy and local cyan edge, no fandom imagery | Stronger display emphasis; less quiet than B; no claim it improves retention |

Recommendation: **B**, as a design judgment, not user-research evidence. Optional C material refinements
can be discussed, but a hybrid is NOT silently approved or implemented. Violet remains for actual AI
contexts; no AI content exists in this fixture, so no arbitrary violet decoration was added.
Large panel radius24 and existing palettes/effect styles remain shared. No detached raster layout.

## Action Preservation Contract

This matrix carries every Status inventory ID forward; it is not an interaction test.
R04 proposes the visual priority. R05/R06 must specify missing hit areas/dialog states before R07.
No row below authorizes deleting a callback, mode data, storage or action.

| Inventory IDs | Proposed destination / state | Current evidence limit |
| --- | --- | --- |
| STA-01 | Visible primary button -> existing Cycle route | Rendered in all six; no prototype route/save wiring |
| STA-02 | Week summary and Calendar bottom item -> Calendar | Week data/entry represented; real date event is unchanged |
| STA-03 | Both mode datasets in scroll: Today/todos/week/XP/details | Merge proposal needs owner approval; hidden swipe is not the only planned access |
| STA-04, STA-05 | Profile bottom item -> existing avatar/name handlers | Equivalent entry proposed; avatar/name editing dialogs belong R06, not deleted |
| STA-06 | Add task button below todos -> existing AddTask | Native linked button rendered; form/validation/cancel belong R05 |
| STA-07 | Four todo rows with checked/open states | Check glyphs are editable fixture text, not wired checkbox controls yet |
| STA-08, STA-09 | Keep reorder and removal in todo row management | Separate touch targets/row menu not drawn in this direction sketch; required R05/R08 detail, not approved hide |
| STA-10 | Existing child data preserved; advanced microtask access remains G06 | Do not invent a functioning entry or close the existing gap |
| STA-11 | Optional level-up notice state, not everyday content | Specify reduced-motion/acknowledge later R14/R15 |
| STA-12 | Error/loading retry state -> refreshForCurrentDay | Training fixture has no load error; required alternate state in R05/R08 |
| STA-13 | Existing Cycle/Profile settings path | Dormant legacy callback is not promoted to a fake button |

The no-plan/rest/recovery/deload/completed/unknown decisions stay domain-owned. The comparison
contains training with unknown readiness only. R05 must draw other states, preserve explanations,
and not turn unknown into an invented score or force training for XP. G01-G06 are still open.

## Evidence and Approval

[Small comparison](../remaster/evidence/R04-small-comparison.png),
[normal comparison](../remaster/evidence/R04-normal-comparison.png),
[R04 evidence](../remaster/evidence/R04.md). Browser canvas captures show whole fixed frames at Figma
zoom; font sizes were not viewport-scaled. Prototype viewer captures are window-dependent,
not exact Android screenshots. No device/fontScale/TalkBack, user study or motion test.

Owner choice: **pending**. Approved node/revision: **none**. Allowed production structural changes:
**none yet**. Request an explicit A/B/C choice (or precise requested changes). Record the chosen
small/normal nodes and R04-v1, scope of changes and remaining R05/R06 states in DECISIONS/FIGMA_MAP.
Keep alternatives; finish R04 only after that approval. Do not automatically run R05.
