# Performance Optimization Status

Checkpoint: 2026-07-26  
Device: realme RMX3710, Android 15 / API 35  
Branch: `codex/overnight-premium-hud-polish`

## Stable work completed

- Removed redundant startup database maintenance and moved remaining maintenance off the critical startup path.
- Avoided restarting reactive Statistics flows on every resume.
- Deferred beta/usage analytics until the bottom Statistics item is composed.
- Removed an unused, potentially unbounded per-exercise weight-history join from the Statistics critical path.
- Moved Statistics aggregation and UI mapping off the main thread.
- Reduced redundant beta SharedPreferences writes and schedule-flow resubscriptions.
- Cached expensive Compose drawing primitives for the status backdrop and annual chart.
- Added a `:baselineprofile` module, generated release baseline/startup profiles, and added repeatable cold-start and Statistics navigation macrobenchmarks.
- Reduced first-screen Statistics text layout nodes by combining styled title/body and value/subtitle pairs.
- Removed the full `StatisticsViewModel` from `ProfileScreen`; personal metrics now reuse the existing Status flow and update use cases instead of starting Statistics history and aggregation streams.
- Deferred `WorkoutViewModel`, its active-workout/schedule streams, and workout-settings activity launchers on Profile until workout settings are explicitly requested.
- Bounded Statistics workout sessions to the 56-day progress-proof window with a 200-session safety cap, using the indexed timestamp range.
- Bounded annual progression history to the earliest active plan through today, while preserving one pre-plan baseline per exercise and collapsing same-session sets to their maximum weight.

Latest verification: `:app:compileDebugKotlin` and focused Room checks passed; 299 local unit/guard tests passed with 0 failures (1 skipped); both Baseline Profile benchmark variants compile.

## Current benchmark baseline

Macrobenchmark target: non-minified release, 5 iterations unless the OEM metric returned fewer valid samples.

| Scenario | Compilation | Median | Tail / jank |
|---|---:|---:|---:|
| Cold start | None | 936.5 ms | runs 899–1138 ms |
| Cold start | Baseline Profile | 952.5 ms | valid runs 952–953 ms |
| Open Statistics | None | p50 121 ms | p90 250 ms; jank 75% |
| Open Statistics | Baseline Profile | p50 69 ms | p90 121 ms; jank 40% |
| Open Statistics after text-layout optimization | Baseline Profile | p50 61 ms | p90 117 ms; jank 40%; worst p90 129 ms |
| Open Profile after removing Statistics pipeline | None | p50 69 ms | p90 150 ms; jank 69.23% |
| Open Profile after removing Statistics pipeline | Baseline Profile | p50 29 ms | p90 93 ms; jank 21.43% |
| Open Profile after deferring WorkoutViewModel | Baseline Profile | p50 29 ms | p90 81 ms; jank 16.67% |

Interpretation:

- Cold start is comfortably below Android Vitals' excessive cold-start threshold of 5 seconds, but the profile does not show a reliable cold-start improvement on this device.
- The profile is valuable for Statistics: p50 improved by 43%, p90 by 52%, and median jank by 47% compared with no compilation.
- The profile is also valuable for Profile navigation: p50 improved by 58%, p90 by 38%, and median jank by 69% compared with no compilation.
- Deferring the workout pipeline kept Profile p50 stable while improving p90 by another 13% and median jank by 22%.
- Statistics is still not smooth. At 60 Hz the frame budget is about 16.7 ms; current 61/117 ms percentiles are slow frames, though far below the 700 ms frozen-frame threshold.
- Profile is materially better with compilation but is not yet smooth at 29/93 ms. Its worst measured Baseline trace is dominated by a 52.6 ms Compose recompose, 39.5 ms measure, 19.3 ms draw, and concurrent GC pressure.

## Trace findings

Worst uncompiled Statistics transition frame:

- `Choreographer#doFrame`: 195.5 ms
- traversal: 176.7 ms
- measure / `AndroidOwner:onMeasure`: 119 ms
- 36 `TextStringSimpleNode::measure` slices: 43.4 ms total
- draw / `AndroidOwner:draw`: about 37–38 ms
- concurrent GC: 92 ms

This points to first Compose measurement, text layout, allocation pressure, and draw cost—not a blocking Room query—as the immediate UI bottleneck.

Profile after deferring `WorkoutViewModel`:

- worst representative Baseline frame: 79.8 ms
- measure / `AndroidOwner:onMeasure`: 50.9 ms
- 33 `TextStringSimpleNode::measure` slices: 23.1 ms total
- recomposition during measure: 17.3 ms
- draw / `AndroidOwner:draw`: 18.4 ms
- concurrent GC: 102.4 ms

## Rejected experiments

- Collapsing or reordering the first report did not produce a useful measured gain and was reverted.
- Consolidating all `techSurface` background/highlight/border work into one Path draw pass worsened p50 and jank and was reverted.
- Combining `SystemSettingsRow` title/subtitle into one annotated Text improved Profile p90 from 81 to 77 ms, but worsened p50 from 29 to 31 ms and jank from 16.67% to 21.43%; it was reverted.

## Next work

1. Reduce Profile first-content text/measure cost without changing its visual hierarchy; keep only experiments that improve the five-run benchmark.
2. Continue reducing first-screen Statistics measure cost without hiding report content; validate every UI experiment with the existing macrobenchmark.
3. Add release CI performance runs on a stable emulator/reference device; the Realme OEM Perfetto trace lacks some modern frame slices, so the local suite uses legacy startup and `gfxinfo` frame metrics.
4. Re-check profile size/coverage after the next architecture changes and regenerate with `:app:generateBaselineProfile`.

## Commands

```powershell
.\gradlew.bat :app:generateBaselineProfile
.\gradlew.bat :baselineprofile:connectedNonMinifiedReleaseAndroidTest `
  '-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark' `
  '-Pandroid.testInstrumentationRunnerArguments.class=com.ihor.thesystem.baselineprofile.StartupBenchmarks'
.\scripts\check-quick.cmd
```

Macrobenchmark installs and removes the target package. Do not run it on a device whose local app data must be preserved without making a backup first.
