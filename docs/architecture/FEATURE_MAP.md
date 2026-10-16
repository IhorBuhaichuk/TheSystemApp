# Feature And Data Flow Map

Перевірено 2026-09-20, `36d6c480da90438c1b5adaf363de7cfb6986f9d8` + верифіковані P02-P03 working diffs.
Ця карта деталізує [GRAPH_REPORT](../../GRAPH_REPORT.md), а не замінює код.
Проблеми мають ID з [PROJECT_AUDIT](../../PROJECT_AUDIT.md).

## Легенда

Scoped2026-10-15 audit: [R05 controls/data flow](../design/R05_DATA_FLOW.md),17 screen
states and DF01-DF10. Use its aliases for new setup/partial/session-only contracts.
Current wiring fixes: `A/feature/status/ui/RpgTodoBlock.kt` parameterless add ->
StatusViewModel.onAddTaskTap(0); `A/core/ui/UiErrorEffect.kt` consumes ShowError in
Status/Profile/WorkoutDialogHost while STARTED. Persistent critical failure state is
still R09. No schema/domain change from this audit; P02/P03 data work is preserved.

- `A` = `app/src/main/java/com/ihor/thesystem/`
- `D` = `domain/src/main/java/com/ihor/thesystem/domain/`
- `T` = `app/src/test/kotlin/com/ihor/thesystem/`
- `I` = `app/src/androidTest/java/com/ihor/thesystem/`
- Шлях після префікса є точним шляхом у репозиторії.
- «Реалізовано» означає простежений code path, не гарантію runtime-якості.
- «Частково» означає незамкнену взаємодію; не всю функцію слід переписувати.

## Суть продукту

Локальна система щоденного вибору тренування, виконання й прогресу з RPG-подачею.
Status відповідає «що сьогодні», Cycle відповідає «як виконати/налаштувати», Calendar
відповідає «коли», Statistics відповідає «що змінилося», Profile відповідає «хто я/мої налаштування».
AI Architect є додатковим пояснювачем і джерелом пропозицій, а не власником тренувальної істини.

```mermaid
flowchart TD
  O["Onboarding: profile / equipment / config"] --> S["Status: Today Order"]
  E["Cycle: schedule editor"] --> SCH["Schedule + exercises"]
  SCH --> DEC["DecideTodayWorkout"]
  CAL["Calendar: day overrides"] --> DEC
  HC["Optional Health Connect sleep"] --> DEC
  R["Readiness entries or neutral fallback"] --> DEC
  DEC --> S
  S -->|"primary action: navigate"| CY["Cycle: workout dialog"]
  CY --> WD["Durable workout draft"]
  WD --> CY
  CY --> FIN["FinalizeSession"]
  FIN --> LOG["Session logs + sets"]
  FIN --> XP["Quest completion + XP + rank"]
  LOG --> ST["Statistics / progress proof"]
  LOG --> DEC
  XP --> S
  LOG --> AI["Architect / local insight"]
  AI --> VAL["ValidateDirectives"]
  VAL -->|"accepted recommendations"| PM["Progression / directives"]
  PM --> CY
  O -. "GAP: no starter schedule creation" .-> SCH
```

Остання пунктирна стрілка є відсутнім очікуваним зв'язком, не реалізованою функцією.
CalendarCycle day override і тренувальний Schedule є різними сутностями, не двома UI одного DAO.

## Реалізовані області

| Область | Що реально є | Стан / межа |
| --- | --- | --- |
| Onboarding | 5 кроків, ім'я/ціль/equipment/experience/cycle, completion route | Частково: config без starter schedule; completion поза Room |
| Status | player/XP, Today Order, quests, todos, рекомендація, режими | Реалізовано; decision warnings не повністю відображені |
| Today decision | recovery debt, readiness, calendar OFF/recovery, deload, no-excuse, training | Rule-based domain; не медична модель і не AI-прогноз |
| Cycle/System | редагування днів, вправи, picker, active workout | Реалізовано; тут виконується workout |
| Workout | inputs за tracking mode, completed flags, durable draft, idempotent finish report | Draft переживає resume/rotate/process recreation; один draft completion ID створює один session/reward |
| Equipment | profile, allowed equipment, exercise substitution | Підключено до onboarding та recommendations |
| Exercise catalog | assets, переклади, versioned technique metadata | Каталог не дорівнює готовій програмі |
| Calendar | місяць/дата, денний summary, todos, cycle settings | Окремий day-type overlay; потрібні cross-feature тести |
| Quests/RPG | main/side quests, tasks, XP, streak, ranks, rollover | Один ACTIVE quest не нагороджується повторно; regeneration є окремим ризиком |
| Statistics | weekly summary, workout proof, weight/annual progression, beta block | Queries bounded; не кожен query повертає всю історію |
| Annual plans | local step-loading generation, manual editor, details | Окремий local flow; matrix та encoded plan note |
| Profile | player/body metrics, profile/avatar editing, settings | Shared StatusViewModel; settings через WorkoutViewModel |
| AI Architect | weekly insight, suggestions, fallback, history | Release Gemini disabled; частина модулів UI недоступна |
| Health Connect | availability/permissions, on-demand sleep | Лише READ_SLEEP; rationale entry не реалізований |
| Manual readiness | models, DAO/repository, calculation, reading | Частково: немає підключеного save/check-in UI |
| Nutrition | entry/DAO/repository/use cases, floor calculations | Частково: save use case без виклику з feature UI |
| Backup | JSON export, preview/confirmation, validation, transaction | Не повний перенос стану; partial merge/cascade ризик |
| Beta metrics | unique open/refresh days, decision/day, weekly counts | Local snapshots; не cohort analytics і не feedback channel |
| Background | DailyResetWorker та foreground refresh | Shared day sync; потрібні concurrency/time-boundary tests |

## Власники даних

| Джерело істини | Storage | Читачі й письменники |
| --- | --- | --- |
| Профіль | player, weight_log | Player use cases, Status/Profile, progression |
| Конфігурація | system_config, equipment_profile | onboarding, cycle/settings, decision |
| Програма | schedule, workout_templates, cross refs, exercises | Cycle editor, picker, GenerateDailyQuests, WorkoutViewModel |
| Calendar overlay | calendar_cycle_config, calendar_cycle_day | CalendarSettings, daily summary, Today decision |
| Readiness/nutrition | readiness_entries, nutrition_entries | decision/statistics; UI writes ще не завершено |
| Квести | quest, quest_task, quest_log | GenerateDailyQuests, CompleteQuest, rollover, status/stats |
| Виконання | workout_session_logs, exercise_set_logs | FinalizeSession commit-or-return-existing за unique completionId, set editors, statistics, AI context |
| Чернетка тренування | workout_drafts, workout_draft_sets | WorkoutDraftRepository, WorkoutViewModel; не є session log і не дає XP |
| Навантаження | progression_matrix, reference_matrix, protocol_template, workout_directives | recommendations, finalization, validated AI, annual planner |
| Денний список | todo | status/calendar та daily summaries |
| AI history | chat_message_table | Architect repository/VM |
| Setup/metrics/backup status | SharedPreferences | onboarding, beta recorder, BackupRepositoryImpl |
| Тимчасовий стан | StateFlow/in-memory | selected date і dialogs; workout edits мають durable Room snapshot |

```mermaid
flowchart LR
  ES["exercise_sets"] -->|"FK sessionId / CASCADE"| WS["workout_sessions"]
  ESL["exercise_set_logs"] -->|"FK sessionId / CASCADE"| WSL["workout_session_logs"]
  CCD["calendar_cycle_day"] -->|"FK cycleId / CASCADE"| CCC["calendar_cycle_config"]
  WDS["workout_draft_sets"] -->|"FK draftId / CASCADE"| WD["workout_drafts"]
  ESL -. "logical exerciseId" .-> EX["exercises"]
  PM["progression_matrix"] -. "logical exerciseId" .-> EX
  QT["quest_task"] -. "logical questId" .-> Q["quest"]
  WSL -. "logical questId" .-> Q
  Q -. "logical scheduleId" .-> SC["schedule"]
  CROSS["workout_exercise_cross_ref"] -. "logical IDs" .-> EX
  CROSS -. "logical IDs" .-> WT["workout_templates"]
```

Це вибрані зв'язки, не повний ERD. Суцільні стрілки перелічують **усі чотири** FK schema53.
Пунктирні зв'язки підтримує код. `@Relation` сам по собі FK не створює.

## Критичні послідовності

### First launch

```mermaid
sequenceDiagram
  participant Shell as AppEntry/NavGraph
  participant Pref as OnboardingRepository
  participant Seed as DatabasePopulator
  participant UI as Onboarding VM
  participant UC as CompleteOnboarding
  participant DB as Room repositories
  Shell->>Pref: observe completed flag
  Pref-->>Shell: false -> Onboarding
  Seed->>DB: singleton rows + exercise metadata (async)
  UI->>UC: validated answers
  UC->>DB: update player, equipment, config
  UC->>Pref: completed=true
  Pref-->>Shell: Status
  Note over UC,DB: No starter schedule write or cross-store atomic commit
```

Status очікує DatabaseReadiness; route flag не доводить наявності повного профілю/розкладу.
Часткові writes, повтор після аварії та prefs-only restore потребують окремих тестів.

### Workout finish

```mermaid
sequenceDiagram
  participant UI as Workout dialog
  participant VM as WorkoutViewModel
  participant UC as FinalizeSession
  participant DB as Room transaction
  participant AI as AI or local fallback
  participant Gate as ValidateDirectives
  UI->>VM: edit / complete sets
  VM->>DB: atomic draft snapshot (no XP/logs)
  DB-->>VM: restore only for same date and stable plan identity
  UI->>VM: finish
  VM->>UC: stable draft completionId + session + sets
  UC->>DB: commit-or-return session; if new, progress + quest/XP; close draft
  DB-->>UC: committed sessionId + created/existing
  UC->>AI: report suggestions
  AI-->>UC: response or fallback
  UC->>Gate: validate against system context
  Gate-->>UC: allowed directives
  UC->>DB: save allowed directives/targets when applicable
  UC-->>VM: AI report or committed local report
  VM-->>UI: report dialog
```

Canonical completion і optional enrichment навмисно є різними фазами. Unique `completionId` захищає
session/sets на рівні Room; side effects і draft close виконуються лише в canonical transaction.
Збій AI/directives після commit повертає локальний report із persisted session ID, а не помилку повтору.

### Daily state and AI

Foreground refresh / DailyResetWorker -> SyncTodayState -> FinalizeDay або GenerateDailyQuests
-> repositories -> flows -> Status/Calendar/Statistics.
Worker не гарантує запуск рівно опівночі; foreground має наздоганяти пропущені дні.

Architect -> SendArchitectAnalysis -> AiArchitectRepository -> parser -> UI proposals.
ApplyAiRecommendations -> ValidateDirectives -> accepted matrix writes.
AnnualProgressionPlan має окремий локальний Generate/Save flow; не називати його віддаленим AI.

## Маршрути задач

Для UX/Figma/Compose remaster використовуй [UX_CONTRACT](../design/UX_CONTRACT.md) та
[ACTION_INVENTORY](../design/ACTION_INVENTORY.md): R02, 2026-10-12, точковий static UI read
цього HEAD + local diff. Там поточні callbacks і planned states окремо, відкриті UX-R02-G01..G06.
Це не оновлення audited revision усієї карти або доказ виправлення цих gaps.

Спочатку відкрий 2-4 entry files з потрібного рядка. Розширюй читання лише за залежностями.
Тести вже існують, якщо явно не позначено «додати»; скорочені test names шукай під `T`.

| Симптом / задача | Entry files | Перевірка |
| --- | --- | --- |
| Не той перший route | `A/core/navigation/AppEntryViewModel.kt`; `D/usecase/OnboardingUseCases.kt`; `A/data/repository_impl/OnboardingRepositoryImpl.kt` | `T/domain/usecase/OnboardingUseCasesTest.kt`; додати Room+prefs restore integration |
| Після setup немає тренування | `D/usecase/OnboardingUseCases.kt`; `D/model/Onboarding.kt`; `A/data/local/room/database/DatabasePopulator.kt` | onboarding tests + clean-install schedule/Today Order integration |
| Неправильне Today рішення | `D/usecase/DecideTodayWorkoutUseCase.kt`; `D/usecase/CalculateReadinessUseCase.kt`; `D/usecase/CalculateRecoveryDebtUseCase.kt` | `T/domain/usecase/DecideTodayWorkoutUseCaseTest.kt`; calculation tests |
| Текст/CTA не відповідає рішенню | `A/feature/status/viewmodel/TodayOrderUiMapper.kt`; `A/feature/status/ui/RpgTodayOrderBlock.kt`; `A/feature/status/ui/StatusScreen.kt` | `T/feature/status/viewmodel/TodayOrderUiMapperTest.kt`; route UI test |
| Неправильний день/опівніч | `D/usecase/SyncTodayStateUseCase.kt`; `D/usecase/FinalizeDayUseCase.kt`; `D/usecase/ResolveTrainingCycleDayUseCase.kt` | однойменні тести в `T/domain/usecase/`; timezone/concurrency cases |
| REST, але є workout quest | `D/usecase/GenerateDailyQuestsUseCase.kt`; `D/usecase/DecideTodayWorkoutUseCase.kt`; `A/feature/status/viewmodel/WorkoutViewModel.kt` | GenerateDailyQuests + decision tests; додати OFF cross-flow case |
| Втрата/дублі workout sets | `A/feature/status/viewmodel/WorkoutViewModel.kt`; `D/model/WorkoutDraft.kt`; `D/repository/WorkoutDraftRepository.kt`; `A/data/local/room/dao/WorkoutDraftDao.kt`; `D/usecase/FinalizeSessionUseCase.kt`; `A/data/local/room/dao/WorkoutAnalyticsDao.kt` | WorkoutViewModelMappersTest; WorkoutDraftCompatibilityTest; FinalizeSessionUseCaseFallbackTest; `I/data/repository_impl/WorkoutDraftRepositoryImplTest.kt`; `WorkoutFinishIdempotencyIntegrationTest.kt` |
| Редагування однієї вправи | `D/usecase/LogWorkoutSetsUseCase.kt`; `D/usecase/StatisticsUseCases.kt`; `D/repository/WorkoutAnalyticsRepository.kt`; `A/data/local/room/dao/WorkoutAnalyticsDao.kt`; `A/data/repository_impl/ProgressionMatrixRepositoryImpl.kt` | explicit `sessionId + exerciseId`; `T/domain/usecase/LogWorkoutSetsUseCaseTest.kt`; `T/data/repository_impl/ProgressionMatrixRepositoryImplTest.kt`; `I/data/local/room/WorkoutAnalyticsEditIntegrationTest.kt` |
| XP/ранг/серія | `D/usecase/CompleteQuestUseCase.kt`; `D/model/PlayerProgressionPolicy.kt`; `D/usecase/RecalculateGlobalRankUseCase.kt` | однойменні tests у `T/domain/`; completed quest regeneration |
| Вправи/обладнання | `D/usecase/EquipmentProfileUseCases.kt`; `D/usecase/GenerateDailyQuestsUseCase.kt`; `A/feature/exercise_search/viewmodel/ExerciseSearchViewModel.kt` | EquipmentProfileUseCasesTest; ExerciseSearchViewModelTest |
| Calendar/settings | `A/feature/calendar/viewmodel/CalendarViewModel.kt`; `A/feature/calendar/viewmodel/CalendarSettingsViewModel.kt`; `D/usecase/GetDailySummaryForDateUseCase.kt` | CalendarSettingsViewModelTest; GetDailySummaryForDateUseCaseTest |
| Statistics/chart | `D/usecase/GetStatisticsDataUseCase.kt`; `A/feature/statistics/viewmodel/StatisticsViewModel.kt`; `A/data/local/room/dao/WorkoutAnalyticsDao.kt` | BuildProgressProofsUseCaseTest; WorkoutAnalyticsQueryGuardTest; SQL fixtures |
| Annual plan | `D/usecase/GenerateAnnualProgressionPlanUseCase.kt`; `D/usecase/SaveAnnualProgressionPlanUseCase.kt`; `D/usecase/GetAnnualProgressionDetailsUseCase.kt` | AnnualProgressionPlanViewModelTest; GetAnnualProgressionDetailsUseCaseTest |
| Profile/edit | `A/feature/profile/ui/ProfileScreen.kt`; `A/feature/status/viewmodel/StatusViewModel.kt`; `D/usecase/PlayerUseCases.kt` | PlayerUseCasesTest; PlayerProfileValidationPolicyTest; ProfilePerformanceGuardTest |
| Health Connect | `A/health/HealthConnectPermissions.kt`; `A/data/repository_impl/HealthConnectSignalsRepositoryImpl.kt`; `app/src/main/AndroidManifest.xml` | додати sleep/permission tests + real HC rationale launch |
| Backup | `D/usecase/BackupUseCases.kt`; `A/data/repository_impl/BackupRepositoryImpl.kt`; `A/feature/status/viewmodel/WorkoutViewModel.kt` | BackupUseCasesTest; BackupImportPolicyTest; додати Room round-trip |
| AI/error/fallback | `A/data/remote/ai/AiAvailability.kt`; `A/data/repository_impl/AiArchitectRepositoryImpl.kt`; `A/feature/architect/viewmodel/ArchitectViewModel.kt` | AiAvailabilityTest; AiErrorClassifierTest; ArchitectViewModelTest; FinalizeSessionUseCaseFallbackTest |
| AI mutation | `D/usecase/ValidateDirectivesUseCase.kt`; `D/usecase/ApplyAiRecommendationsUseCase.kt`; `D/usecase/FinalizeSessionUseCase.kt` | ValidateDirectivesUseCaseTest; ApplyAiRecommendationsUseCaseTest/GuardTest |
| Beta metrics | `D/usecase/BetaMetricsAggregator.kt`; `D/usecase/GetBetaMetricsUseCase.kt`; `A/data/repository_impl/BetaMetricsRepositoryImpl.kt` | `T/domain/usecase/BetaMetricsAggregatorTest.kt`; schedule/date fixtures |
| Seed/startup | `A/TheSystemApp.kt`; `A/core/di/DatabaseModule.kt`; `A/data/local/room/database/DatabasePopulator.kt` | DatabasePopulatorCoreMetadataTest/GuardTest; `:baselineprofile`; cold/first-install окремо |
| Insets/swipes | `A/core/navigation/AppNavGraph.kt`; `A/core/ui/components/SystemBottomNavBar.kt`; target screen | `I/ui/ResponsiveLayoutTest.kt`; додати real NavHost journey |
| Колір/матеріал | `A/core/theme/SystemTokens.kt`; `A/core/ui/components/SystemPanels.kt`; `A/core/ui/components/SystemGlassComponents.kt` | compile + screenshots; `docs/playbooks/UI_POLISH.md` |
| Room migration | entity/DAO + `A/data/local/room/database/DatabaseMigrations.kt`; `A/data/local/room/database/AppDatabase.kt` | check-room + `I/data/local/room/database/AppDatabaseMigrationTest.kt` |
| Build/release/lint | `app/build.gradle.kts`; `gradle/libs.versions.toml`; `.github/workflows/android-ci.yml` | compile/tests/lint/bundle; merged manifest + release BuildConfig |

## Правила змін

- Domain вирішує; UI відображає state і передає events. Не дублювати Today rules у Compose.
- Logs впливають на XP, recovery, annual history, beta metrics та AI context.
- Schedule впливає на quests, calendar, recommendations і planned/missed metrics.
- Seed version і Room schema version незалежні; seed не повинен перезаписувати user logs.
- Backup change має врахувати FK, prefs, export destination та route readiness разом.
- Зміна AI prompt без parser/validation/fallback tests є неповною.
- Source guards не доводять runtime correctness або відсутність glyph clipping.
- Не читати весь FEATURE_MAP для color-only task: це індекс, не великий обов'язковий prompt.

## Підтримка карти

Оновлюй ревізію лише після перевірки відповідних частин. Неверифіковані області позначай явно.
Новий файл додай у routing row; не дублюй код. Після task зафіксуй зміну контракту й tests.
Аудит не стає актуальним автоматично після виправлень. Жодна карта не замінює diff і тести.
