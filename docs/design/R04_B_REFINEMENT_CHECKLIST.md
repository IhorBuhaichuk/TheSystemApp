# R04 / B Refinement Checklist

Updated: 2026-10-14. Current owner request supersedes the unfinished A/B/C comparison.
Earlier D24 chose B placement only; **D29 explicitly accepts the current initial Status in full**.
R04 is done/2026-10-14. Pending labels/art appearance below are resolved by the current-as-shown approval.
Do not start R05 automatically or change Kotlin. Start at [Selected Design / D29](R04_SELECTED_DESIGN.md),
then the relevant row here. All10 chosen icons/W02 are applied to both phone sizes.
Full-content canvas overview and cleanup completed; both Present top/bottom checks repeated.
Candidate-generation descriptions/board links below are historical: boards44:768/44:1499
and4 obsolete demos were removed. Exact source/evidence remains local. Do not regenerate options.
Progress-details disposition approved/D27: remove from all3 Status frames, information moves
to Statistics later (R06/R12). Applied in Figma, no Kotlin/data change; lower gap24px verified.

## Context / Sources

- Figma file: `Gjpbz08rE8dVNvQhHp9vHT`; main0:1/overview188:178,
  phones190:210, masters190:211. Main overview390x946 is not a phone viewport.
- Selected B small: [39:683](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=39-683), 360x640.
- Selected B normal: [39:1084](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=39-1084), 390x844.
- [R04 prompt](../remaster/prompts/06-R04-three-design-directions.md), [event parity](R04_REWORK_CONTRACT.md),
  [action inventory](ACTION_INVENTORY.md), [foundation tokens](R03_FOUNDATIONS.md), [Figma map](FIGMA_MAP.md).
- Header reference: `C:/Users/gesha/OneDrive/Зображення/Знімки екрана/Знімок екрана (400).png`.
- Button reference: `C:/Users/gesha/Temp/codex-clipboard-ca12d7f0-437a-4033-a123-5acdaf3e0fe2.png`.
- Both supplied screenshots have been visually read. Marker percentages describe image opacity,
  not text opacity. Reference buttons are bright filled cyan + restrained outlined glass.

## Work Order / Evidence

| ID | State | Exact requirement / next evidence |
| --- | --- | --- |
| B01 | owner_approved / D29 | Initial B placement/D24, later explicit current Status acceptance/D29; production behavior/other screens not approved here |
| B02 | verified | A/C and rejected B-v1 removed. D26 cleanup removes2 candidate boards/2 R03 demos too;1 main frame,2 phone previews,10 library roots retained. Nav masters44:705/44:747 on page190:211; instances144:156/48:236 preserved |
| B03 | owner_approved / D29 | Keep the current visible «Статус» heading; no title substitution. Internal route unchanged |
| B04 | generated | Five separate transparent PNG concepts H01-H05 generated and shown in chat; files in assets/r04-b/headers. Nothing inserted into selected header |
| B05 | N/A in accepted baseline | D29 accepts the header as shown, without any H01-H05 artwork. Historical alpha imperfections remain evidence, not a pass; only a later explicit artwork request would reactivate mask QA |
| B06 | verified / D28 | `Цей тиждень` and `1 / 3 виконаних тренувань` absent from all3 frames; dates/states retained, current PNG/Present inspected |
| B07 | selected / applied | W02 revision2 date tickets/D26-D28, phone nodes183:168/192:102.7 dates/states preserved; no counter; actual overview/Present inspected |
| B08 | selected / applied | TIME04/D26 in both metadata groups183:154/176:154; native render inspected |
| B09 | selected / applied | EX03 revision3 repetition loop/D26, both metadata groups; not the older EX03 geometry |
| B10 | selected / applied | XP01/D26; icon has XP, adjacent text75 only. Corrected small caption39:733 and verified Present after restart |
| B11 | owner_approved / placement verified | D29 accepts current button/material appearance; shared Filled/ButtonRaised/textPrimary preserved. CTA below View plan atY400, nav fixed. Technical native binding/contrast/accessibility checks remain R07/R08/P19 |
| B12 | owner_approved / D29 | Current copy `Рекомендація на день` / `Не вистачає вихідних даних.` accepted as shown. HC/manual-readiness source limits unchanged; other decision states belong to R05/R08 |
| B13 | owner_approved / D29 | Keep current title «Справи»; no automatic TODO01 substitution |
| B14 | selected / applied | TASK03/D26 in both headings184:154/178:154 |
| B15 | evaluated | Keep48x48 plus target and existing plus action; no shrinking of the hit area. Final chosen section icon/copy may change its visual balance |
| B16 | source_checked | Explain overflow menu honestly: current callbacks support removal and reorder, not an already implemented todo editor. Any proposed edit command must be marked future, not shipped |
| B17 | owner_approved / visual updated | Eight12x12 handle vectors removed from four rows in each B size; menu dots/checkboxes/text retained. Primary gesture is row-wide long-press roughly1-1.5s, then drag up/down. Preserve onTodosReordered and todo identity; Kotlin implementation belongs to R08 |
| B18 | selected / applied | CHECK02/D26: all8 phone artworks replaced,2 done/2 open each; todo identity/text/menus retained |
| B19 | selected / applied | NAV-STATUS05/D26 through both nav masters |
| B20 | selected / applied | NAV-CALENDAR01/D26 through both nav masters |
| B21 | selected / applied | NAV-SYSTEM01/D26, label «Тренування»/D25 through both masters; route Cycle unchanged |
| B22 | selected / applied | NAV-STATS05/D26 through both nav masters |
| B23 | selected / applied | NAV-PROFILE04/D26 through both nav masters |
| B24 | inspected | Stable IDs above every icon and week option in actual exports. H01-H05 are chat/file choices, not artwork inserted into the screen |
| B25 | accepted design / scroll verified | D29 accepts current appearance; D27/D28 captures show4 todos,24px end gap, fixed nav and selected icons. Native material/contrast/accessibility proof remains R07/R08/P19; no Android/fontScale/TalkBack claim |
| B26 | closure_verified | D29 recorded; prompt/PROGRESS/QUEUE/handoff/evidence synchronized. Host checks/source-preservation result in R04 evidence. R05 not started; no Kotlin/build/device/push |

Checkbox meaning: use [x] only with evidence; owner choice is separate from creation.
There are **50 icon options + 5 week options + 5 artwork options**. Do not silently
reduce the counts or substitute repeated variants. Native vectors/official icon
assets are preferred for icons; image generation is for the five raster artworks.

## Owner Choice Ledger

| Item | Options | Selected |
| --- | --- | --- |
| Composition | B small/normal above | B, placement approved 2026-10-13 |
| Page title | TITLE01 Мій день; TITLE02 План дня; TITLE03 Огляд дня; TITLE04 Сьогодні; TITLE05 День у фокусі | Current «Статус», approved as shown/D29; alternatives not selected |
| Header | H01-H05 | Current header without artwork approved as shown/D29; do not insert unchosen H options |
| Week | W01-W05 | W02 revision2 date tickets, applied/D26 |
| Duration | TIME01-TIME05 | TIME04, applied/D26 |
| Exercises | EX01-EX05 | EX03 revision3, applied/D26 |
| Experience | XP01-XP05 | XP01, applied/D26;75 only beside icon |
| Recommendation title/copy | Рекомендація на день / Не вистачає вихідних даних. | Current copy approved as shown/D29 |
| Todo block title | TODO01 Завдання на день; TODO02 Список на сьогодні; TODO03 Мої завдання; TODO04 Щоденні кроки; TODO05 План дій | Current «Справи», approved as shown/D29; alternatives not selected |
| Todo block icon | TASK01-TASK05 | TASK03, applied/D26 |
| Checkbox | CHECK01-CHECK05 | CHECK02, applied/D26 |
| Status icon | NAV-STATUS01..05 | NAV-STATUS05, applied/D26 |
| Calendar icon | NAV-CALENDAR01..05 | NAV-CALENDAR01, applied/D26 |
| System icon | NAV-SYSTEM01..05 | NAV-SYSTEM01, applied/D26 |
| Third tab label | Тренування / Мій план / План розвитку | Тренування, approved2026-10-13/D25; Figma only |
| Statistics icon | NAV-STATS01..05 | NAV-STATS05, applied/D26 |
| Profile icon | NAV-PROFILE01..05 | NAV-PROFILE04, applied/D26 |
| Progress details location | Status / workout summary / Statistics | Statistics, approved D27; Status Figma box removed; native R08/R12 remains future |
| Weekly counter | «1 / 3 виконаних тренувань» | Remove from Status, approved/applied D28; keep dates/training marks and underlying metrics |

## Verified Behavior / No Invented Actions

- STA-01 Open workout -> current Cycle/System entry, not immediate session start.
- View plan -> existing Cycle entry; changing button paint does not change destination.
- STA-02 week/calendar -> Calendar; selected date parameter is still G02, not implemented by Figma.
- STA-06 plus -> AddTask, with confirm/cancel; G05 remains open.
- STA-07 checkbox -> actual todo identity, not training completion/XP proof.
- STA-08 reorder is functional, despite the owner not wanting its visible handle.
  Owner clarified: primary access is long-press anywhere on the todo row, then
  drag up/down; no dedicated handle. Approximate1-1.5s hold is a UX preference to
  validate during R08/native device tests, not a claim of an implemented timer.
  Preserve ordinary checkbox/menu taps, vertical scrolling and cancel behavior.
  TalkBack/keyboard move-up/move-down semantic actions remain an accessibility
  alternative; they do not replace the owner's requested primary gesture.
  Actual Compose gesture wiring belongs to R08, prototype detail to R05.
- STA-09 current remove callback. Editing a task is not established by this contract.
- Health Connect `SleepSessionRecord` can supply duration when another source has
  written sleep and read permission is granted; the app itself is not a sleep tracker.
- Today decision also uses schedule, calendar overrides, history/recovery and readiness.
  Missing signals must not produce a fabricated score or claim of measured recovery.

## Technical Checkpoint / Before Cleanup

Native R03 surface instances replace six large workout backgrounds. Text/accent,
background, normal border and plate/button/panel gradient stops have semantic
bindings; remaining decorative/raw colors must be reviewed, not declared all bound.
Both nav masters `44:705`/`44:747` are now at page root; B small uses144:156 at
X12/Y552/W336/H88, normal48:236 at X12/Y756/W366/H88, Fixed. Their internal
groups39:797/39:1065 Scroll with parent to avoid nested fixed rendering issues.
Duplicate normal reserve markers44:689/44:686 were removed after checking
X0/Y1022/W1/H1; one44:698 remains. Small B now has one72:136 at the same
position, as a scrolling child (not an extra visible element).
Two accidentally pasted page-level text nodes61:136/62:136 were removed;
normal copy was then edited in the existing native text layers and inspected.
Historical prototype captures/URL changes were inconsistent. Round2 explicit
flows and actual top/bottom captures now verify placement/vertical scrolling. Figma browser edits
work despite MCP write quota. Do not repeatedly retry blocked MCP calls.

## Historical Option Assets

Native boards below were removed after D26 selection; use local exports/source only for history.
Active nodes and captures: [Selected Design](R04_SELECTED_DESIGN.md).

- [Week board44:1499](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=44-1499), page X8700/Y5600, 410x960; [PNG](assets/r04-b/week-options.png).
- [Icon board44:768](https://www.figma.com/design/Gjpbz08rE8dVNvQhHp9vHT?node-id=44-768), page X8000/Y5600, 616x1760; [PNG](assets/r04-b/icons-50.png).
- Actual B context screenshots: [small](../remaster/evidence/screenshots/r04-b/B-small-context.jpg), [normal](../remaster/evidence/screenshots/r04-b/B-normal-context.jpg). These are Figma editor captures, not Android screenshots.
- Header concepts: [H01 compass](assets/r04-b/headers/H01.png), [H02 planner](assets/r04-b/headers/H02.png), [H03 route](assets/r04-b/headers/H03.png), [H04 workout](assets/r04-b/headers/H04.png), [H05 journal](assets/r04-b/headers/H05.png).
- Official vector provenance: google/material-design-icons, revision49d4db35df873165d6bd6ba09b063c7dafbac2f4, Apache-2.0. Sources/license/manifest live in assets/r04-b/icons; account_box is the later replacement for NAV-PROFILE05. No Android dependency added.

R04 closed by D29. Next task is R05 after separate authorization; native material/contrast checks
remain in R07/R08/P19, and unchosen artwork has no active alpha-mask gate in this accepted baseline.
Owner can select incrementally by TITLE/TODO/H/W/TIME/EX/XP/TASK/CHECK/NAV IDs;
creation is not approval. Do not make the owner select all60 in one reply.
Here H01-H05 are R04-B header artwork IDs, not the older UX_CONTRACT H01-H06
hierarchy proposals; do not confuse these two namespaces.

At a pause, update the row being worked on, exact node IDs and the next unfinished
ID here and in QUEUE/handoff. Do not erase historical evidence or fabricate completion.
