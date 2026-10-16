# R04 Rework Contract

Date: 2026-10-13. Owner feedback rejects R04-v1 (1/10). No v1 direction is approved.
The owner authorized revising design proposals and aligning documentation, not production Kotlin.
Read this file before R04_DIRECTIONS: the latter describes rejected historical v1 frames.
Latest override D24: B placement is approved. Follow [B refinement checklist](R04_B_REFINEMENT_CHECKLIST.md)
for pending detail choices; A/C were removed on owner request after preserving shared masters.
The three-composition brief below is historical rework context, not permission to recreate A/C.
Latest D29/2026-10-14 accepts the current initial Status after D26-D28 in full; R04 done.
Current titles/copy/header/material appearance are frozen as shown in [Selected Design](R04_SELECTED_DESIGN.md).
No unchosen H01-H05 artwork is inserted. Earlier pending clauses below describe the pre-approval review;
R05 interactions/other states and R07/R08/P19 native verification remain separate work.

## Approved Corrections / Not a Final Frame Approval

- Keep Status heading and date; remove redundant user name from the date and the duplicate Today heading.
- Keep the meaning and visual direction of Open workout and View plan controls.
- Replace ambiguous Readiness heading with a brief explanation about today's training load.
  Missing signals are a compact honest note, not an empty large scoring panel. Actual restrictive
  recovery/deload decisions must remain visible before starting, never hidden to make a cleaner mockup.
- Week is a seven-cell calendar component with distinct selected/completed/planned/rest states,
  dates and non-color cues. No plain concatenated day string. Day selection does not activate a plan.
- Todos are separate native rows: checkbox, title, duration, management/reorder affordance.
  Two of four are checked in the synthetic fixture; no impossible completion count.
- Add todo is a plus icon with accessible name and at least48x48 touch target, not a full-width action.
- Real weekly progress is primary. Rank, XP and RPG attributes are secondary/optional details;
  removing visible redundant metadata does not delete stored data or remove Profile/Statistics access.
- Every navigation item owns its icon and individually centered native label in one equal-width cell.
  Preserve all five route labels and order. No single text string pretending to be five nav labels.

## Three Different UX Compositions

1. A / Action-first: session summary and primary CTA high in content, separate weekly/todo sections.
2. B / Weekly rhythm: calendar context first, then day's session/actions; no new date mutation or
   unconditional training CTA for historical/rest days. Same today fixture across all variants.
3. C / Daily agenda: training and habits in a structured timeline; primary CTA near the lower
   thumb zone, without stealing bottom navigation or hiding safety notes. Still one primary action.

Differences must be visible in section order, grouping and CTA position, not font changes alone.
Same synthetic facts/actions, same source tokens and shared components. Native editable Figma,
no web UI, complete-screen raster or automatic implementation of three themes.

## Event Parity

| Visible control / state | Existing owner / destination | Forbidden semantic change |
| --- | --- | --- |
| Open workout | STA-01, Status onStartWorkout -> Routes.Cycle | No auto-start, auto-completion or domain decision in UI |
| View plan | Same Cycle entry in current fixture | No guessed new route or duplicate commit |
| Calendar week / Calendar tab | STA-02, onOpenCalendarTap -> Calendar | Do not claim a date parameter exists; G02 remains open |
| Plus | STA-06, onAddTaskTap -> AddTask, confirm/cancel retained | Do not use todo count as invented questId; G05 unresolved |
| Checkbox | STA-07, onTodoToggled with actual todo identity | Not workout completion or quest reward proof |
| Row menu / row long-press drag | STA-08/09, onTodosReordered / onRemoveTodo | Latest owner clarification: no visible handle; hold the row about1-1.5s then drag up/down. Preserve identity, cancellation, checkbox/menu taps and deletion paths |
| Advanced child-task entry | STA-10, existing G06 dormant contract | Do not pretend already wired or drop child data |
| Profile tab / identity editing | STA-04/05, existing Profile avatar/name handlers | Keep picker cancel, validation and dialog dismiss |
| Both former dashboard mode datasets | STA-03, merged visible access proposal | No deletion of level/attributes/quest data |
| Level-up notice / retry | STA-11/12, acknowledge / refreshForCurrentDay | Actual alternate states, not synthetic saved/error claims |
| Workout settings | STA-13 via existing Cycle/Profile path | No fake legacy callback button |
| Remaining bottom tabs | Existing Routes.Cycle/Statistics/Profile navigation | Keep labels/order/back stack semantics |

Figma hit areas are concept wiring only. Production event parity and all day/error/restore states
must be verified in their R05/R06/R08 owners. G01-G06 are not closed by any design approval.

## Review Gates

Native small360x640 and normal390x844 frames, untruncated Ukrainian copy,48-unit icon targets,
primary action above/clear of navigation, actual aligned nav cells, readable calendar/todo states.
Screenshot each complete candidate and inspect lower content. Record exact native node links.
Owner chose B placement after reviewing v2; v1 recommendation B remains withdrawn.
Earlier gate required B refinements and explicit final owner approval, not composition choice alone.
D29 supplies that final initial-screen approval; native tests and unimplemented interaction/state gates
are not claimed passed. See R04 evidence for closure and the accepted-baseline artwork disposition.
