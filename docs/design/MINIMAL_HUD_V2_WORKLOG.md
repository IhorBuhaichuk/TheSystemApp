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
- Keep Roboto and sentence case. Preserve legitimate abbreviations such as XP and AI.
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
| 3 | Status | In progress | `StatusScreen.kt`, `RpgStatusDashboard.kt`, `RpgTodayOrderBlock.kt`, `RpgTodoBlock.kt` |
| 4 | Calendar | Pending | `CalendarScreen.kt` |
| 5 | System/Cycle | Pending | `CycleScreen.kt` |
| 6 | Statistics | Pending | `StatisticsScreen.kt`, overview sections, chart components |
| 7 | Profile | Pending | `ProfileScreen.kt` |
| 8 | Dialogs and state panels | Pending | shared dialogs and changed workout/report dialogs |
| 9 | Accessibility/performance regression | Pending | tests and screenshot artifacts |
| 10 | Plain-language Ukrainian copy | In progress | presentation mappers, top-level screens, dialogs |

## Verification ledger

- [x] Baseline screenshots for five tabs
- [ ] Baseline screenshots for representative dialogs
- [x] Baseline `gfxinfo` captured on Realme
- [x] `scripts/check-quick.cmd`
- [x] `scripts/check-web-ui-guard.cmd`
- [ ] relevant unit and Compose/UI tests
- [ ] small phone
- [ ] normal phone
- [ ] font scale 1.3
- [ ] five after screenshots and changed dialogs
- [ ] `git diff --check`
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

## Current checkpoint

- Branch: `codex/overnight-premium-hud-polish`
- Base product commit: `78ccd52`
- Concept commit: `be7172b`
- Baseline: `artifacts/design-implementation/minimal-hud-v2/baseline/`
- Next action: finish the Status density/motion pass, then remove screen-specific
  blur and repeating decorative work before rolling the surface hierarchy across
  the remaining tabs.
