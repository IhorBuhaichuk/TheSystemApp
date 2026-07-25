# THE SYSTEM: LEVEL UP - prompts to 10/10

This is a sequential execution package for Codex. Run one prompt per task, review the result, commit it, and only then start the next prompt. Do not run prompts that touch the same files in parallel.

## Model routing

- **GPT-5.6 Sol**: complex, ambiguous, high-risk domain work, performance, final release audit.
- **GPT-5.6 Terra**: everyday implementation, bounded refactors, UI consistency, tests.
- **GPT-5.6 Luna**: clear and repeatable work such as CI setup, cleanup, extraction, and documentation.
- Use the lowest reasoning level that is sufficient. Most prompts do not need Max or Ultra.

## How the external feedback changes the plan

Accepted:

- The generic RPG fitness concept is crowded; the differentiator must be the recovery-aware daily decision engine, reliable workout logging, local-first trust, and eventually Shadow Clone Protocol.
- English localization, a real beta, CI, maintainability work, distribution, and monetization are necessary.
- Large Compose/ViewModel files are a long-term regression risk.

Corrected priorities:

- Engineering quality is not the same as release readiness. Small-screen CTA reachability, clipped text, startup time, lint, release build, permissions, and backup safety come before new flagship features.
- Room schema version 50 is not itself a product defect. Migration discipline after real users receive the app is what matters.
- Moving tests and deleting dead files are maintenance tasks, not beta blockers.
- Full social feeds, guilds, and leaderboards should wait until the core loop proves D7 retention. A shareable weekly result can come earlier.
- Shadow Clone should be built only after logging and recovery rules are reliable, so it amplifies the product instead of hiding core friction.

---

## Prompt 1 - CI and non-negotiable quality gates

**Recommended model: GPT-5.6 Luna, Medium reasoning.**

```text
You are a senior Android build/release engineer.

Repository: C:\Users\gesha\AndroidStudioProjects\TheSystem-master

First read:
- GRAPH_REPORT.md
- AGENTS.md
- docs/playbooks/BUGFIX.md
- app/build.gradle.kts
- settings.gradle.kts
- gradle/libs.versions.toml, if present
- scripts/check-quick.cmd
- scripts/check-tests.cmd
- scripts/check-room.cmd
- scripts/check-web-ui-guard.cmd

Task:
Add lightweight CI that protects every push and pull request.

Requirements:
- Add a GitHub Actions workflow using the JDK/Gradle versions required by this repository.
- Cache Gradle safely and cancel superseded runs on the same branch.
- Run Kotlin compile, unit tests, architecture/Compose guards, Room guards, Android lint, and a release bundle compile where credentials are not required.
- Do not call Windows .cmd wrappers directly on a Linux runner. Reuse their underlying checks or add a small cross-platform entry point.
- Upload test/lint reports and the unsigned release artifact when a job fails or completes.
- Never place signing keys, API keys, or Gemini credentials in the workflow.
- Keep jobs understandable and avoid an expensive matrix until the beta justifies it.

Verify locally with the closest available checks. Update GRAPH_REPORT.md only if build architecture changes. Report changed files, executed checks, and any CI limitation. Do not push or merge.
```

## Prompt 2 - P0 small-screen usability and navigation

**Recommended model: GPT-5.6 Sol, High reasoning.**

```text
You are a senior Jetpack Compose engineer focused on regression-safe responsive UI.

First read:
- GRAPH_REPORT.md
- AGENTS.md
- docs/playbooks/BUGFIX.md
- UI_UX_GUIDELINES.md
- app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt
- app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt
- app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt
- the top-level Calendar, System/Cycle, Statistics, and Profile screens

Known failures:
- At roughly 360x640 dp the Today Order primary CTA can be below the viewport.
- A vertical swipe can trigger Status mode switching instead of scrolling.
- The bottom navigation can consume too much height.
- Calendar/System/Profile contain clipped text on smaller screens.

Task:
Fix these as a bugfix pass, not a redesign.

Requirements:
- Preserve content, hierarchy, actions, visual identity, and domain logic.
- Make every primary CTA reachable on 360x640 dp and common larger phones.
- Horizontal mode switching must require a clearly horizontal gesture and must not steal vertical scroll.
- Respect navigation/inset height so content is not hidden.
- Prevent text clipping at font scales 1.0 and 1.3 without using viewport-scaled fonts.
- Add stable test tags and focused Compose UI tests for CTA reachability, scrolling, tab switching, and representative text visibility.
- Keep shared visual effects in tokens/components.

Verify all five tabs on emulator at small and normal phone sizes. Run check-quick, relevant UI tests, and check-web-ui-guard. Do not push or merge.
```

## Prompt 3 - Release, privacy, backup, and permissions hardening

**Recommended model: GPT-5.6 Terra, High reasoning.**

```text
You are an Android release hardening engineer.

First read:
- GRAPH_REPORT.md
- docs/playbooks/BUGFIX.md
- app/src/main/AndroidManifest.xml
- app/build.gradle.kts
- Health Connect permission/repository files
- backup export/import UI and use cases
- ArchitectScreen.kt and AI availability handling
- PRIVACY_POLICY.md
- STORE_LISTING.md
- docs/HEALTH_CONNECT_RATIONALE.md

Task:
Produce a verifiable beta release candidate without changing product scope.

Requirements:
- Fix all Android lint errors, including locale-sensitive formatting.
- Declare only Health Connect permissions that the shipped core loop actually consumes. If steps, heart rate, or exercise sessions are not used in decisions/UI, remove or defer those permissions and align docs.
- Add backup import preview, explicit confirmation, validation, and a non-destructive failure path.
- When Gemini is disabled, show an honest useful local fallback state without raw errors or a dead premium shell.
- Verify release build configuration contains no key or secret and no medical claims.
- Keep AI suggests, system decides; ValidateDirectivesUseCase remains the gatekeeper.

Run lint, check-tests, check-room, check-web-ui-guard, and bundle/assemble release. Update release/privacy docs when behavior changes. Report a GO/NO-GO result and exact blockers. Do not push or merge.
```

## Prompt 4 - Cold-start performance and startup correctness

**Recommended model: GPT-5.6 Sol, High reasoning.**

```text
You are a senior Android performance engineer.

First read:
- GRAPH_REPORT.md
- app startup/Application/AppEntry files
- DatabasePopulator.kt
- Room database creation and migration files
- Hilt startup modules
- any existing benchmark or baseline-profile configuration

Known observation:
- Representative cold start was about 5.3 seconds, with an even slower first post-install display.
- Core metadata appears to be reapplied on startup.

Task:
Profile and reduce startup latency without risking user data.

Requirements:
- Measure cold, warm, and first-install startup before editing.
- Identify the main-thread and database contributors using real traces where possible.
- Make metadata seeding/version updates idempotent and versioned; do not rewrite unchanged rows every launch.
- Move non-critical initialization off the first frame while preserving deterministic onboarding/route decisions.
- Add a Macrobenchmark/Baseline Profile only if it provides measurable value and fits the existing modules.
- Add regression tests for seeding/version behavior and clean-install startup state.

Acceptance:
- Demonstrate a material measured improvement on the same emulator/device, ideally at least 40%, or explain the remaining platform limit with evidence.
- No lost user data, duplicate seed data, route flash, or delayed Today Order correctness.

Run targeted tests plus check-tests and report before/after measurements. Do not push or merge.
```

## Beta gate after Prompt 4

Start a 5-10 person closed beta now. Continue the next prompts while beta users exercise the current loop. Do not wait for every polish item before collecting real feedback.

## Prompt 5 - Closed-beta feedback and evidence loop

**Recommended model: GPT-5.6 Terra, Medium reasoning.**

```text
You are a product engineer preparing a privacy-first closed beta.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- MVP_DEFINITION.md
- existing BetaMetrics models/use cases/repository/UI
- backup/export implementation
- privacy and store documents

Task:
Make a 7-14 day beta measurable without third-party analytics.

Requirements:
- Preserve existing local beta metrics and avoid Firebase/Amplitude/Segment.
- Add a user-controlled diagnostics export containing app version, device/API summary, local beta metric aggregates, and sanitized failure information. Never export workouts, health values, names, notes, or identifiers without explicit selection.
- Add an in-app beta feedback action with five short questions: Today Order clarity, logging friction, decision trust, RPG motivation, and next-day return intent.
- Create docs/BETA_PLAYBOOK.md and docs/BETA_RESULTS_TEMPLATE.md with recruitment, consent, test scenarios, daily check-in, bug severity, and exit criteria.
- Define evidence thresholds for onboarding completion, first workout, week-one mission completion, D1/D7 return, crashes, and trust.
- Do not add social features or a backend.

Add tests for redaction and metrics export. Run check-tests and check-web-ui-guard. Do not push or merge.
```

## Prompt 6 - Personalization integrity

**Recommended model: GPT-5.6 Sol, High reasoning.**

```text
You are a Kotlin domain engineer specializing in training logic.

First read:
- GRAPH_REPORT.md
- docs/playbooks/NEW_FEATURE.md
- onboarding goal/config use cases
- CalculateRecommendedSetUseCase.kt
- workout prescription/session models
- tests for onboarding and recommended sets

Known issue:
- Onboarding stores goal-specific sets/reps, but recommendation code still contains generic hardcoded 3x12 and 3x8 paths.

Task:
Make onboarding choices truthfully affect prescriptions.

Requirements:
- Domain owns goal, experience, equipment, and exercise-specific recommendation rules.
- Replace hidden hardcodes with named, tested prescription policies for strength, hypertrophy/general fitness, and endurance where supported.
- Respect equipment availability and prior performance.
- Preserve safe deterministic defaults when data is missing.
- Explain the applied rule in UI state without moving business logic into Compose.
- Version recommendation policy if historical behavior or reports depend on it.
- Do not claim medical personalization.

Use table-driven unit tests across goals, experience levels, equipment profiles, missing history, and edge values. Run targeted tests and check-tests. Update GRAPH_REPORT.md if ownership/contracts change. Do not push or merge.
```

## Prompt 7 - Recovery-aware decision engine v2

**Recommended model: GPT-5.6 Sol, Extra High reasoning.**

```text
You are a senior Kotlin domain architect with conservative fitness-safety judgment.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- DecideTodayWorkoutUseCase.kt
- CalculateRecoveryDebtUseCase.kt
- CalculateReadinessUseCase.kt
- HealthSignals.kt
- related tests and TodayDecision UI mapping

Known issue:
- Heavy-load/recovery logic relies on absolute tonnage thresholds, including a fixed 8000 kg-style cutoff, which does not personalize well.

Task:
Evolve the decision engine from absolute thresholds to explainable relative signals.

Requirements:
- Use a rolling personal baseline where enough history exists; otherwise use conservative deterministic defaults.
- Normalize training load by the user's own recent sessions and supported signals such as completion, RPE/performance trend, recovery debt, and optional sleep.
- Produce decision type, confidence/data-quality level, concise reasons, expected consequence/reward, and safe fallback.
- Keep no-AI behavior complete. AI must not mutate decisions.
- Avoid false precision and medical/injury diagnosis.
- Version the rules and make boundary behavior deterministic.
- Do not require Health Connect.

Write exhaustive table/property-style tests for new user, sparse data, sudden load spike, missed sessions, deload, poor sleep, unavailable health data, and conflicting signals. Run check-tests and document rule changes. Do not push or merge.
```

## Prompt 8 - Workout session durability and zero-loss logging

**Recommended model: GPT-5.6 Terra, High reasoning.**

```text
You are a senior Android state/persistence engineer.

First read:
- GRAPH_REPORT.md
- docs/playbooks/BUGFIX.md
- WorkoutViewModel.kt
- ActiveDayCard.kt
- WorkoutDialogHost.kt
- WorkoutReportDialog.kt
- FinalizeSessionUseCase.kt
- workout Room entities/DAOs/repositories

Task:
Make active workout logging resilient and fast.

Requirements:
- Restore an in-progress session after process death or app recreation.
- Make set completion and workout finalization idempotent.
- Never lose already confirmed set data because of timer, navigation, rotation, or backgrounding.
- Preserve all existing logging/action paths.
- Keep one-tap common logging, clear completed/incomplete states, and an unambiguous finish CTA.
- Final report must show completed work, progress, system verdict, and next-session effect.
- Compose consumes ViewModel state; no business logic in UI.

Add repository/use-case/ViewModel tests for process recreation, duplicate finish, interrupted timer, partial session, and retry. Add a focused UI smoke test. Run check-room if schema changes, then check-tests. Do not push or merge.
```

## Prompt 9 - Shadow Clone Protocol as the flagship differentiator

**Recommended model: GPT-5.6 Sol, High reasoning.**

```text
You are a product-minded Kotlin domain engineer and Compose interaction designer.

Precondition:
Prompts 6-8 are complete and stable.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- docs/playbooks/NEW_FEATURE.md
- workout history/progression models and use cases
- active workout state/UI
- any existing Shadow Clone design notes

Task:
Implement Shadow Clone Protocol: a local, deterministic comparison against the user's best relevant prior performance.

Requirements:
- Define what makes a prior set/session comparable: same exercise, equipment, unit, and compatible prescription.
- Show a subtle target/ghost for load, reps, volume, or pace without blocking logging.
- Never encourage unsafe load, override Today Order/recovery, or punish an intentionally lighter day.
- Handle no history, changed equipment, deload/recovery day, unit changes, ties, partial sessions, and personal bests.
- Keep it optional and feature-flagged during beta.
- Domain computes comparison and result; Compose only renders UI state.
- Add concise celebration, not a distracting game layer.

Add domain, ViewModel, persistence, and focused UI tests. Update GRAPH_REPORT.md. Run check-room if needed and check-tests. Do not push or merge.
```

## Prompt 10 - English localization and accessibility

**Recommended model: GPT-5.6 Terra, High reasoning.**

```text
You are a senior Android internationalization and accessibility engineer.

First read:
- GRAPH_REPORT.md
- UI_UX_GUIDELINES.md
- docs/playbooks/UI_POLISH.md
- all string resources and representative screens/dialogs

Task:
Make the complete core loop production-ready in Ukrainian and English.

Requirements:
- Extract user-visible hardcoded strings from Compose/ViewModels where appropriate.
- Add complete values/strings.xml and values-uk/strings.xml or values-en/strings.xml with a clear default-locale strategy.
- Use plurals, format arguments, locale-safe dates/numbers, and content descriptions.
- Never use translated display strings as business identifiers.
- Cover onboarding, Status/Today Order, active workout, report, Calendar, System, Statistics, Profile, backup, Health Connect, and AI-unavailable states.
- Verify 360x640 dp and common phone size at font scales 1.0, 1.3, and 2.0 for the core loop.
- Provide TalkBack semantics, minimum touch targets, contrast, and non-color-only status cues.
- Preserve the existing sci-fi identity and hierarchy.

Add locale/formatting tests and focused accessibility UI tests. Run lint, check-tests, and check-web-ui-guard. Do not push or merge.
```

## Prompt 11 - Maintainability without behavior change

**Recommended model: GPT-5.6 Terra, High reasoning.**

```text
You are a senior Kotlin refactoring engineer.

First read:
- GRAPH_REPORT.md
- AGENTS.md
- docs/playbooks/BUGFIX.md
- RpgStatusDashboard.kt
- CycleScreen.kt
- CalendarScreen.kt
- WorkoutViewModel.kt
- tests covering these files

Task:
Reduce high-risk god files without changing product behavior or visuals.

Requirements:
- Establish or strengthen characterization tests before moving code.
- Split by existing ownership: screen assembly, stateless sections, UI models/mappers, gesture behavior, and state coordination.
- Do not create a generic abstraction without real reuse.
- Keep ViewModel state/events stable; extract coordinators/use cases only when they remove actual responsibility.
- Move pure domain tests into :domain only when dependencies remain clean and test execution becomes simpler.
- Remove ExampleUnitTest.kt and Translator.kt only after proving they are unused.
- Keep each change reviewable and avoid formatting churn.

Acceptance:
- Same screenshots/semantics for representative states.
- No lost UI blocks, logging, navigation, or analytics.
- Architecture guards and full tests pass.

Update GRAPH_REPORT.md with new file ownership. Do not push or merge.
```

## Prompt 12 - Retention and lightweight distribution, not a social network

**Recommended model: GPT-5.6 Terra, High reasoning.**

```text
You are an Android product engineer focused on respectful retention.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- beta results/metrics documents
- Calendar/cycle scheduling
- existing WorkManager and notification code

Task:
Improve week-one retention without feeds, leaderboards, guilds, or cloud accounts.

Requirements:
- Add opt-in, user-scheduled reminders for Today Order and planned sessions.
- Handle Android notification permission, timezone, rescheduling, missed days, and disabled permissions honestly.
- Deep-link reminders to the relevant local screen.
- Never use guilt, fake urgency, or notifications on recovery/rest days unless explicitly requested.
- Add a privacy-safe shareable weekly result card containing only user-selected summary data.
- Add a first-week checklist only if beta evidence shows orientation friction.
- Track effects through existing local beta metrics.

Add scheduling, timezone, permission, and deep-link tests. Run check-tests and emulator smoke checks. Update privacy/store docs if needed. Do not push or merge.
```

## Prompt 13 - Ethical monetization and Google Play Billing

**Recommended model: GPT-5.6 Sol, High reasoning.**

```text
You are a senior Android monetization architect. Optimize long-term trust and retention, not short-term paywall conversion.

Precondition:
Read BETA_RESULTS.md or the filled beta template. If there is no evidence that users complete onboarding, understand Today Order, log a workout, and return, create only the strategy/entitlement design and do not activate a paywall.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- MVP_DEFINITION.md
- privacy/store docs
- AI availability/cost boundaries
- current official Google Play Billing and policy documentation

Task:
Create MONETIZATION_STRATEGY.md and, only when beta evidence is sufficient, implement a testable entitlement layer.

Recommended tiers:
- Free forever: Today Order, safe recovery rules, core logging, history, backup, and no-AI fallback. No ads.
- System Pro: Shadow Clone, advanced analytics, deeper cycle customization, premium themes, and richer exports.
- Architect+: optional AI insights with a separate price/credit boundary so variable AI cost cannot damage margins.

Requirements:
- Do not paywall safety, data export, basic recovery explanations, or user-owned history.
- Use Google Play Billing with restore purchases, pending/cancelled states, offline grace behavior, localization, and test products.
- Do not hardcode prices; display Play-provided localized pricing.
- Prefer server-side verification before public subscriptions. If backend is unavailable, keep purchases in test/feature-flag mode and document the blocker.
- Add entitlement and billing-state tests; no raw billing errors in UI.

Run check-tests and release checks. Update privacy/store docs. Do not push or merge.
```

## Prompt 14 - Final 10/10 release audit

**Recommended model: GPT-5.6 Sol, Max reasoning. Use once, after all previous work.**

```text
You are the final release owner, product reviewer, and Android QA lead.

First read:
- GRAPH_REPORT.md
- PRODUCT_STRATEGY.md
- MVP_DEFINITION.md
- AGENTS.md
- UI_UX_GUIDELINES.md
- BETA_RESULTS.md or completed beta results template
- privacy, store, Health Connect, backup, and monetization docs

Task:
Audit the actual app, fix remaining release-blocking defects, and produce docs/PUBLIC_RELEASE_REPORT.md.

Required verification:
- Clean install and completed-onboarding upgrade path.
- Ukrainian and English.
- 360x640 dp, normal phone, and one larger device; font scales 1.0, 1.3, 2.0.
- All five tabs, Today Order CTA, active workout, process death restore, report, backup preview/import, Health Connect denied/unavailable, no-AI release state, notifications disabled, and purchase restore if enabled.
- Cold/warm startup and obvious jank.
- check-tests, check-room, check-web-ui-guard, lint, release bundle, installable release candidate, and Android vitals readiness.
- No secrets, medical claims, inaccessible critical action, clipped core text, data-loss path, or dead feature shell.

Report:
- GO/NO-GO.
- P0 blockers, P1 first patch, P2 later.
- Measured performance and test evidence.
- Beta KPI evidence versus release thresholds.
- Exact generated AAB/APK path and checksum when available.

Do not invent successful checks. Fix only clear P0/P1 release defects; defer feature expansion. Do not push or merge.
```

---

## Monetization recommendation

### Product model

1. **Free core with no ads.** The daily decision, safe recovery rules, workout logging, history, backup, and local fallback are the trust contract.
2. **System Pro.** Start with a regional test around **199 UAH/month** or **1,499 UAH/year** in Ukraine; test a global price around **USD 5.99/month** or **USD 49.99/year**. Use Play-localized prices, not currency conversion in code.
3. **Architect+ or AI credits.** Test around **349 UAH/month** only after a backend and measured AI unit economics exist. Do not include unlimited expensive AI in a cheap lifetime plan.
4. **Founder lifetime offer.** A limited **999-1,499 UAH** offer for the first 100-300 users can fund launch, but it must exclude future variable-cost AI.
5. **Later, not now.** Coach/team licenses can become a second business line after consumer retention is proven.

Avoid banner/interstitial ads. They conflict with the premium tactical identity, distract during workouts, and provide little revenue at early scale.

### Scenario model

Assumptions:

- Blended paid price: **249 UAH/month**.
- Store deduction planning factor: **15%**.
- Figures below are after the store deduction only, before tax, refunds, AI/backend, support, and acquisition.

| Monthly active users | Paid conversion | Paying users | Gross/month | After store/month |
|---:|---:|---:|---:|---:|
| 1,000 | 2% | 20 | 4,980 UAH | 4,233 UAH |
| 10,000 | 3% | 300 | 74,700 UAH | 63,495 UAH |
| 50,000 | 4% | 2,000 | 498,000 UAH | 423,300 UAH |
| 100,000 | 5% | 5,000 | 1,245,000 UAH | 1,058,250 UAH |

### Honest income expectation

- Before product-market fit: **0-10,000 UAH/month** is normal.
- With roughly 3,000-15,000 engaged MAU and 2-3% conversion: approximately **16,000-95,000 UAH/month after store fees**.
- With a strong English launch, 50,000 MAU, 4% conversion, and controlled costs: roughly **423,000 UAH/month after store fees**, before taxes and operating costs.
- A result above **1 million UAH/month** is mathematically possible near 100,000 MAU and 5% paid conversion, but distribution and retention, not code quality alone, determine whether it happens.

The most important commercial metric is not installs. It is the percentage of users who understand Today Order, complete a planned mission, and return during week one.
