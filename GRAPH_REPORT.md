# GRAPH_REPORT.md

Graphify-style architecture map for **THE SYSTEM: LEVEL UP**.

Generated from the current repository structure on 2026-07-25. Use this file as the first context checkpoint before refactoring or adding features, then read only the target files needed for the task.

## High-Level Modules

```text
settings.gradle.kts
├── :domain
│   ├── model/          Pure Kotlin domain models and policies
│   ├── repository/     Repository contracts owned by domain
│   ├── usecase/        Business actions, orchestration, calculations
│   └── util/           Domain utilities, clocks, logging abstractions
└── :app
    ├── core/           Android app shell: DI, navigation, theme, UI primitives, workers
    ├── data/           Room, repository implementations, remote AI adapters
    ├── feature/        Compose screens and Hilt ViewModels by feature
    ├── health/         Health Connect permission integration
    └── presentation/   Shared presentation models/components
```

## Clean Architecture Flow

```text
Compose Screen
  -> Hilt ViewModel
  -> domain/usecase
  -> domain/repository interface
  -> app/data/repository_impl
  -> Room DAO / remote AI / Android service
  -> Room Entity / DTO / mapper
```

Rules of ownership:

- `:domain` must stay free of Android, Room, Compose, Hilt UI concerns, and resource access.
- `:app` owns Android integration: Room database, Hilt modules, Compose UI, navigation, workers, Health Connect, AI clients, and repository implementations.
- Business decisions belong in `domain/usecase` or `domain/model`; persistence details belong in `app/data`.
- Compose screens should talk to ViewModels and UI state, not directly to DAOs or concrete repository implementations.

## Hilt and App Infrastructure

Key Hilt modules live in `app/src/main/java/com/ihor/thesystem/core/di`:

- `DatabaseModule.kt`: creates `AppDatabase`, applies `DatabaseMigrations.ALL_MIGRATIONS`, runs `DatabasePopulator`, exposes DAOs, binds `TransactionProvider`.
- `RepositoryModule.kt`: binds domain repository interfaces to implementations in `data/repository_impl`, including local SharedPreferences-backed lightweight state such as onboarding and beta metrics.
- `AiModule.kt`: binds `AiArchitectRepository`, `WorkoutAnalyticsRepository`, `LiveCoachRepository`, and provides Gemini models/config.
- `NetworkModule.kt`: provides `OkHttpClient` and Coil `ImageLoader`.
- `DispatcherModule.kt`, `AppScopeModule.kt`, `TextProviderModule.kt`: coroutine dispatchers, app scope, and text/context providers.

Entry points:

- `TheSystemApp.kt`: application class.
- `MainActivity.kt`: Android entry point.
- `AppNavGraph.kt`: Compose navigation shell and bottom navigation visibility.

## Room Database Map

Main database:

- File: `app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt`
- Database name: `the_system_db`
- Version: `APP_DATABASE_VERSION = 50`
- `exportSchema = true`
- Type converters: `Converters.kt`
- Migrations: `DatabaseMigrations.kt`
- Seed/population: `DatabasePopulator.kt`

DAO surface exposed by `AppDatabase`:

- Player/profile: `PlayerDao`, `WeightLogDao`, `EquipmentProfileDao`, `NutritionDao`
- System/calendar: `SystemConfigDao`, `CalendarCycleDao`, `ReadinessDao`, `TodoDao`
- Training: `WorkoutDao`, `ScheduleDao`, `WorkoutAnalyticsDao`, `ProtocolTemplateDao`
- Progression/quests: `ProgressionMatrixDao`, `QuestDao`, `QuestLogDao`
- AI/chat: `ChatDao`

Room entity groups:

- Player and body state: `PlayerEntity`, `WeightLogEntity`, `EquipmentProfileEntity`, `NutritionEntryEntity`
- Configuration and daily state: `SystemConfigEntity`, `CalendarCycleConfigEntity`, `CalendarCycleDayEntity`, `ReadinessEntryEntity`, `TodoEntity`
- Exercises and scheduling: `ExerciseEntity`, `DailyTaskTemplateEntity`, `WorkoutTemplateEntity`, `WorkoutExerciseCrossRef`, `ScheduleEntity`, `ScheduleTaskCrossRef`
- Workout execution: `WorkoutSessionEntity`, `ExerciseSetEntity`, `WorkoutDirectiveEntity`, `ExerciseMilestoneEntity`, `WorkoutSessionLogEntity`, `ExerciseSetLogEntity`
- Progression and reference data: `ProgressionMatrixEntity`, `ReferenceMatrixEntity`, `ProtocolTemplateEntity`
- Quests and AI history: `QuestEntity`, `QuestTaskEntity`, `QuestLogEntity`, `ChatMessageEntity`

Repository implementation layer:

- Local data repositories live in `app/src/main/java/com/ihor/thesystem/data/repository_impl`.
- Repository contracts live in `domain/src/main/java/com/ihor/thesystem/domain/repository`.
- Mappers near repository implementations translate Room entities/DTOs into domain models.
- `BetaMetricsRepositoryImpl` stores local beta event snapshots in SharedPreferences, not Room; it does not require a database migration.

## Compose Screen and Navigation Map

Top-level bottom tabs in `AppNavGraph.kt`:

- `Routes.Status` -> `feature/status/ui/StatusScreen.kt`
- `Routes.Onboarding` -> `feature/onboarding/ui/OnboardingScreen.kt` (first-launch flow)
- `Routes.Calendar` -> `feature/calendar/ui/CalendarScreen.kt`
- `Routes.Cycle` -> `feature/cycle/ui/CycleScreen.kt` (System tab)
- `Routes.Statistics` -> `feature/statistics/ui/StatisticsScreen.kt`
- `Routes.Profile` -> `feature/profile/ui/ProfileScreen.kt`

Secondary routes:

- `Routes.CalendarSettings` -> `CalendarSettingsScreen`
- `Routes.Architect` -> `ArchitectScreen`
- `Routes.AnnualProgressionPlan` -> `AnnualProgressionPlanScreen`
- `Routes.AnnualProgressionDetails` -> `AnnualProgressionDetailsScreen`
- `Routes.WorkoutAnalysis` -> `WorkoutAnalysisScreen`
- `Routes.ExercisePicker` -> `ExercisePickerScreen`

Main ViewModels:

- Status/System workout flow: `StatusViewModel`, `WorkoutViewModel`
- First launch: `AppEntryViewModel` selects `Routes.Onboarding` or `Routes.Status`; `OnboardingViewModel` completes initial profile/config setup.
- Calendar: `CalendarViewModel`, `CalendarSettingsViewModel`
- Statistics: `StatisticsViewModel`, `AnnualProgressionDetailsViewModel`
- AI Architect: `ArchitectViewModel`, `WorkoutAnalysisViewModel`, `AnnualProgressionPlanViewModel`
- Exercise search: `ExerciseSearchViewModel`

Shared UI foundation:

- Theme/tokens: `core/theme/SystemTokens.kt`, `Theme.kt`, `Color.kt`, `Dimensions.kt`, `Type.kt`
- Material primitives: `core/ui/components/SystemPanels.kt`, `SystemGlassComponents.kt`, `SystemBottomNavBar.kt`, `SystemDialogComponents.kt`
- Common visual layer: `presentation/common/components/RpgStatusBackdrop.kt`

## Feature Dependency Notes

- Status tab is the daily command center. It combines player status, quests, todos, readiness, workout dialog flow, backup/config dialogs, and today-order logic.
- Onboarding is isolated in `feature/onboarding`; domain owns first-launch state and completion rules through `OnboardingRepository`, `ObserveAppStartDestinationUseCase`, and `CompleteOnboardingUseCase`. App/data persists the completion flag through `OnboardingRepositoryImpl`.
- Calendar tab reads date summaries, todo stats, training/rest markers, and cycle state.
- System tab (`CycleScreen`) presents cycle overview and edits training schedules through `WorkoutViewModel`.
- Statistics tab reads progression matrix, body logs, annual progression, quest and workout proof data. It also displays local beta metrics built by `GetBetaMetricsUseCase` from onboarding state, workout logs, player streak, schedule/config, and `BetaMetricsRepository` event snapshots.
- Local beta metrics are first-party only: no Firebase/Amplitude/Segment. `AppEntryViewModel` records unique opened days through `RecordBetaAppOpenUseCase`; Status/Statistics refreshes also mark the current day idempotently. `RecordTodayOrderDecisionUseCase` records one Today Order decision type per epoch day.
- Architect screens use AI repositories through domain use cases and validation logic. AI output must stay constrained by domain validators before mutating plans.
- Exercise picker is shared by cycle editing and annual progression planning through route source parameters.

## Change Hotspots

Before changing business behavior:

- Read the relevant domain use case first.
- Check repository contract in `domain/repository`.
- Check implementation in `app/data/repository_impl`.
- Check Room schema impact if entities/DAO queries change.
- Check ViewModel UI state mapping before editing Compose.

Before changing Room:

- Update entity/DAO/database version/migration together.
- Keep schema export expectations intact.
- Run Room/schema-related tests where practical.

Before changing Compose UI:

- Read `UI_UX_GUIDELINES.md`.
- Prefer existing `SystemTheme` tokens and `SystemPanel` / `techSurface` primitives.
- Do not bypass ViewModel state or introduce web UI patterns.

## Fast Context Rule

For refactoring or feature work, read this file first, then perform targeted file reads. Do not start with a full repository scan unless this report is stale, missing the area in question, or the task explicitly requires a fresh architecture audit.

## Workflow Files

- `AGENTS.md`: agent roles, context rules, token economy, verification defaults.
- `.codex/instructions.md`: compact boot instructions for Codex sessions.
- `UI_UX_GUIDELINES.md`: aesthetic reference for native Compose UI only.
- `docs/playbooks/UI_POLISH.md`: visual polish workflow.
- `docs/playbooks/ROOM_CHANGE.md`: Room/entity/DAO/migration workflow.
- `docs/playbooks/NEW_FEATURE.md`: feature planning and implementation workflow.
- `docs/playbooks/BUGFIX.md`: bugfix/regression workflow.
- `scripts/check-quick.cmd` / `.ps1`: compile Kotlin quickly.
- `scripts/check-tests.cmd` / `.ps1`: run unit tests.
- `scripts/check-room.cmd` / `.ps1`: run focused Room guards.
- `scripts/check-doc-only.cmd` / `.ps1`: verify documentation-only changes did not touch Kotlin.
- `scripts/check-web-ui-guard.cmd` / `.ps1`: verify the Compose-only UI rule.
