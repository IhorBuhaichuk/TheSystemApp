# Minimal HUD v2 worklog

## Objective

Bring the real Kotlin + Jetpack Compose UI toward the five approved concepts in
`artifacts/design-concepts/minimal-hud-v2/` while improving rendering performance.
Do not change business logic, navigation, product copy, data, or workout behavior.

## Context entry point

Read only:

1. `GRAPH_REPORT.md`
2. `AGENTS.md`
3. `UI_UX_GUIDELINES.md`
4. `docs/playbooks/UI_POLISH.md`
5. this worklog
6. the current target Compose file and the shared primitive it uses

Do not rescan the project unless the architecture or this file is stale.

## Authoritative visual references

- `artifacts/design-concepts/minimal-hud-v2/01-status.png`
- `artifacts/design-concepts/minimal-hud-v2/02-calendar.png`
- `artifacts/design-concepts/minimal-hud-v2/03-system.png`
- `artifacts/design-concepts/minimal-hud-v2/04-statistics.png`
- `artifacts/design-concepts/minimal-hud-v2/05-profile.png`

The current screenshots in `artifacts/design-implementation/premium-hud-v1/after/`
are the implementation baseline, not the target.

## Locked product constraints

- Native Kotlin + Jetpack Compose only.
- Keep current screen, section, navigation, dialog, action, and workout contracts.
- Compose consumes prepared UI state; no product decisions in Composables.
- Keep Roboto and sentence case. Use plain Ukrainian for visible XP/AI wording while
  preserving established internal model, persistence, and serialization names.
- Keep stable status meanings: progress, recovery, danger, reward, neutral.
- Support normal phones, small phones, and font scale 1.3 without overlap or clipping.
- Do not push, merge, or install a review build unless the user has authorized it.

## Minimal HUD v2 visual rules

- One signature hero surface per screen at most.
- Supporting content uses quiet tonal surfaces or grouped rows, not armored frames.
- Depth comes from tone, top-left edge light, ambient shadow, and contact shadow.
- Cyan marks active selection, progress, or the primary action only.
- Violet is reserved for AI/recovery context.
- No screws, decorative bolts, broken rails, disconnected accent strokes, RGB bloom,
  permanent neon halos, or a frame around every item.
- Use whitespace and typography before adding another container.
- Keep controls at least 48 dp where interaction is independent.

## Motion rules

- Press: 90-120 ms depth/scale response, interruptible release.
- State/selection: 180-240 ms.
- Enter/exit: 220-300 ms and only when it explains hierarchy.
- Progress/reward: finite animation that stops drawing frames.
- Prefer draw- or graphics-layer state reads over layout animation.
- Respect the platform animator duration scale.
- No infinite decorative animation in top-level screens.

## Performance baseline risks

Targeted scan on 2026-07-25 found:

- native `BlurMaskFilter` in `RankBadge`, `NeonModifiers`, and three statistics charts;
- repeating coroutine loops in Status UI and an unused glitch text component;
- a layout-phase animated width in `SystemProgressBar`;
- several large custom Canvas surfaces;
- bottom-navigation animation state read during composition;
- heavy shared `techSurface` decoration applied to most panels and plates.

Every risk must be inspected before changing it. Preserve functional timers and exercise
animation behavior; remove or replace only decorative work that is proven unnecessary.

## Device policy

- Realme C55 / RMX3710 is available through
  `C:\Users\gesha\AppData\Local\Android\Sdk\platform-tools\adb.exe`.
- Preserve its app data. Never run data-clearing connected tests on it.
- Use it for baseline/after screenshots, `gfxinfo`, font-scale checks, and safe `install -r`.
- Use a dedicated emulator for destructive instrumentation tests and seeded UI flows.
- Restore any device setting changed for verification.

## Implementation slices

| Slice | Target | Status | Primary files |
|---|---|---|---|
| 0 | Baseline and measurement | Complete | this file, ADB artifacts |
| 1 | Shared tonal surfaces and tokens | Complete | `SystemTokens.kt`, `SystemPanels.kt` |
| 2 | Shared interaction and progress motion | In progress | `SystemMotion.kt`, `SystemGlassComponents.kt`, `SystemBottomNavBar.kt` |
| 3 | Status | Complete | `StatusScreen.kt`, `RpgStatusDashboard.kt`, `RpgTodayOrderBlock.kt`, `RpgTodoBlock.kt` |
| 4 | Calendar | Top-level complete | `CalendarScreen.kt` |
| 5 | System/Cycle | Top-level complete | `CycleScreen.kt` |
| 6 | Statistics | Top-level complete | `StatisticsScreen.kt`, overview sections, chart components |
| 7 | Profile | Top-level complete | `ProfileScreen.kt` |
| 8 | Dialogs and state panels | In progress | shared dialogs and changed workout/report dialogs |
| 9 | Accessibility/performance regression | Pending | tests and screenshot artifacts |
| 10 | Plain-language Ukrainian copy | Complete | presentation mappers, top-level screens, dialogs |

## Verification ledger

- [x] Baseline screenshots for five tabs
- [x] Baseline screenshots for representative dialogs
- [x] Baseline `gfxinfo` captured on Realme
- [x] `scripts/check-quick.cmd`
- [x] `scripts/check-web-ui-guard.cmd`
- [x] relevant unit and Compose/UI tests
- [x] small phone
- [x] normal phone
- [x] font scale 1.3
- [x] five after screenshots and changed dialogs
- [x] `git diff --check`
- [ ] requirement-by-requirement completion audit

## Decision log

- 2026-07-25: Minimal HUD v2 replaces Premium HUD v1 as the visual target.
- 2026-07-25: No new AGSL, Lottie, SharedTransition, or animation dependency.
- 2026-07-25: Canvas remains for meaningful charts, rings, and glyphs only.
- 2026-07-25: Performance changes must preserve current UI state and product behavior.
- 2026-07-25: Realme cold launch reached first frame in 2.445 s and reported fully
  drawn in 4.431 s. A six-tab navigation pass rendered 24 measured frames with
  23 janky frames (95.83%), 300 ms median, and 550 ms p90/p95. This is a diagnostic
  development-build baseline, not a release benchmark, but it confirms that surface
  and animation work must reduce main-thread/draw cost.
- 2026-07-26: Shared armored surfaces were replaced with rounded tonal surfaces and
  a single border/light pass. A warmed ten-tab development-build pass improved the
  median from 300 ms to 125 ms and jank from 95.83% to 81.40%. This is meaningful
  but still far from the 60 fps goal; screen-specific Canvas/blur work remains.
- 2026-07-26: User-facing copy is a dedicated track. Translate at presentation
  boundaries; do not rename persisted template titles, enum values, JSON keys, or
  Room data. XP and product names may remain only where they are established terms.
- 2026-07-26: Known system workout titles use a presentation mapper. Custom names
  remain unchanged. The top-level System and Statistics screenshots are stored as
  `slice-2-system.png` and `slice-2-statistics.png`.
- 2026-07-26: Calendar, System, Statistics, and Profile top-level feeds now use
  keyed lazy sections in the same visual order. Reselecting the active bottom tab no
  longer writes a redundant navigation entry; the Realme rendered zero app frames
  for that no-op tap instead of one 200 ms frame. System scroll remains a debug-build
  hotspot (23.33% jank, 48 ms median) and needs release-profile validation.
- 2026-07-26: Status and System density passes match their approved concept hierarchy
  while retaining existing actions and UI contracts. Calendar, Statistics, and Profile
  received the same top-level pass: compact calendar controls, concept-structured
  statistics rows and metric icons, grouped profile metrics, and quieter white section
  headings. Realme screenshots at font scales 0.9 and 1.3 show no clipping; the scale
  was restored to 0.9 after verification.
- 2026-07-26: Compact edit and add-task dialogs now share one sentence-case header
  and responsive actions. At font scale 1.3 actions stack vertically instead of
  clipping; the add-task dialog remains above the software keyboard through
  `imePadding`. The Realme scale was restored to 0.9 after both checks.
- 2026-07-26: Shared loading, empty, and error panels now provide plain-language
  titles and explanations by default. Top-level loading/error containers use a
  compact content-driven height instead of filling an arbitrary percentage of the
  screen; retry callbacks and UI-state ownership remain unchanged.
- 2026-07-26: The full-screen 260 ms slide between the five primary tabs was removed.
  It forced the outgoing and incoming destinations to draw together and consistently
  added three slow draw frames per switch on the Realme. The warmed minified release
  pass reduced total slow UI-thread frames from 19 to 13 and slow draw frames from
  20 to 15. Selection and press feedback stays in the bottom navigation; hierarchical
  destinations retain a short fade.
- 2026-07-26: The active UI copy audit found no remaining static English display
  strings or all-caps labels. English template keys, exercise filter keys, test tags,
  logs, file formats, and the Health Connect product name remain intentionally
  unchanged. Visible date formatters now use the Ukrainian locale independently of
  the device language. Focused mapper, sentence-case, and Compose-only guard tests
  pass.
- 2026-07-26: Unknown exercise metadata now falls back to plain Ukrainian labels
  such as “Інше” or “Не вказано” instead of exposing internal enum or dataset
  values. User-created exercise names remain unchanged. A focused mapper test guards
  these presentation-only fallbacks.
- 2026-07-26: The debug review build was installed over the existing app without
  clearing data. All five tabs were captured at the normal device size and at a
  temporary 320 dp logical width. Long titles wrap without overlap, bottom-navigation
  labels remain visible, and the device size and 0.9 font scale were restored after
  verification. Bottom-navigation press, selection, and settled frames are stored in
  `review/motion-frames/`.
- 2026-07-26: Interaction motion remains finite and draw-phase based. Progress rings,
  progress bars, cycle-day emphasis, button presses, and bottom-navigation selection
  do not animate layout. The exercise demonstration keeps its functional two-frame
  loop, but now uses one crossfade layer instead of stacking Compose and Coil
  crossfades; changing the exercise also resets the frame deterministically.

## Current checkpoint

- Branch: `codex/overnight-premium-hud-polish`
- Base product commit: `78ccd52`
- Concept commit: `be7172b`
- Baseline: `artifacts/design-implementation/minimal-hud-v2/baseline/`
- Next action: complete the requirement-by-requirement audit. Capture a real workout
  report after the user creates a workout during testing.
