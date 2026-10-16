# 06. Докази, джерела та межі перевірки

Дата аналізу: 2026-10-08. HEAD: `36d6c480da90438c1b5adaf363de7cfb6986f9d8`. Гілка: `codex/overnight-premium-hud-polish`.

До початку були незакомічені P02/P03 зміни, Room schema 52/53, зміни карт і тестів, а також сторонні локальні файли. Аналіз враховує робочу копію. Нічого з цього не відкидалось і не оголошувалось роботою поточного документаційного проходу.

## Рівні впевненості

- **Перевірено в коді:** прочитано відповідний implementation path або конкретний символ.
- **Історичне evidence:** попередній запуск описаний у репозиторії, але тут не повторювався.
- **Ризик:** є кодова передумова, потрібен цільовий test/trace/reproduction.
- **Продуктова гіпотеза:** рекомендація потребує перевірки на людях, не доведена наявністю коду.
- **Публічне джерело:** офіційна документація або опис власника продукту; це не незалежний benchmark конкурента.

## E00. Контекст і карта

- [GRAPH_REPORT](../../../GRAPH_REPORT.md), [AGENTS](../../../AGENTS.md), [FEATURE_MAP](../../architecture/FEATURE_MAP.md).
- [PRODUCT_STRATEGY](../../../PRODUCT_STRATEGY.md), [MVP_DEFINITION](../../../MVP_DEFINITION.md), [UI_UX_GUIDELINES](../../../UI_UX_GUIDELINES.md).
- [PROJECT_AUDIT](../../../PROJECT_AUDIT.md): попередні AUD-ID, не автоматично актуальний список багів.
- [PROGRESS](../../implementation/PROGRESS.md): P01/P02 done, P03 implemented_needs_device; решта не вважається виконаною.

Нове уточнення власника про не-аніме-залежну аудиторію записано в README/04 цього пакета. Воно є напрямом для наступного обговорення; старі стратегічні файли тут не переписано.

## E01. Реальний стек

- [Version catalog](../../../gradle/libs.versions.toml): версії бібліотек.
- [app build](../../../app/build.gradle.kts): SDK 26/36, JVM17, release Gemini disabled, Compose/Room/DI залежності.
- [domain build](../../../domain/build.gradle.kts): Kotlin/JVM, не готовий KMP common module.
- [settings](../../../settings.gradle.kts): app/domain/baselineprofile.
- [gradle.properties](../../../gradle.properties): built-in Kotlin/new DSL opt-out.
- [wrapper](../../../gradle/wrapper/gradle-wrapper.properties): Gradle 9.3.1.
- [Schema53](../../../app/schemas/com.ihor.thesystem.data.local.room.database.AppDatabase/53.json): JSON прочитаний структурно, version53, 30 таблиць.

Секрети `local.properties`, keystore або приватні fitness-дані не читалися. Значення build flags не є доказом перевіреного в цій сесії release APK.

## E02. Onboarding і старт

- [OnboardingUseCases](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt): `CompleteOnboardingUseCase`, `toSystemConfig`, `toPlayer`; немає запису готового Schedule.
- [OnboardingViewModel](../../../app/src/main/java/com/ihor/thesystem/feature/onboarding/viewmodel/OnboardingViewModel.kt): 5 кроків, completed event.
- [OnboardingRepositoryImpl](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/OnboardingRepositoryImpl.kt): SharedPreferences completed flag.
- [DatabasePopulator](../../../app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt): `CORE_METADATA_VERSION`, versioned seed, singleton rows, каталог замість програми.

Підтверджено відсутність створення starter schedule у переглянутому onboarding/seed path. Не стверджується, що користувач не може налаштувати його вручну в Cycle.

## E03. Decision

- [DecideTodayWorkoutUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt): пріоритети recovery/calendar/schedule/deload/no-excuse; `readinessFor`, `plannedWorkoutEpochDays`, `getAllLogs`.
- [GetStatusScreenDataUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatusScreenDataUseCase.kt): об'єднання профілю, квестів, задач, плану, decision; запис beta decision.

Самі формули й пороги не є доказом науково перевіреної моделі персонального тренування. Поточна оцінка не є медичним або тренерським аудитом.

## E04. Readiness і пояснення

- [CalculateReadinessUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt): `BASE_SCORE=70`, optional inputs, levels.
- [TodayOrderUiMapper](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapper.kt): reasons формуються без використання `decision.warnings`; шаблонні duration/outcome.
- [ReadinessRepositoryImpl](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/ReadinessRepositoryImpl.kt): storage save/read.
- [NutritionUseCases](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/NutritionUseCases.kt): save/floor foundation.

Пошук production use sites у `app/src/main/java` і `domain/src/main/java` не виявив підключеного feature check-in write; карта також позначає manual readiness/nutrition частковими. Це не оцінка можливого коду поза цим репозиторієм.

## E05. Workout, draft, finish

- [WorkoutViewModel](../../../app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt): logging, draft restoration, finish gate, backup/settings ownership.
- [FinalizeSessionUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt): transaction, `commit.wasCreated`, draft closure, local report, optional AI, validation.
- [WorkoutAnalyticsDao](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt): `commitSessionOnce`, lookup existing ID, IGNORE insert.
- [WorkoutSessionLogEntity](../../../app/src/main/java/com/ihor/thesystem/data/local/room/entity/WorkoutSessionLogEntity.kt): unique completionId.
- [WorkoutDraftDao](../../../app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutDraftDao.kt): окреме persistence чернеток, не журнал виконаного.
- [P01 evidence](../../implementation/evidence/P01.md), [P02 evidence](../../implementation/evidence/P02.md), [P03 evidence](../../implementation/evidence/P03.md): історичні перевірки.

P03 report зазначає 309 app tests і 4 domain tests та відкладені real-Room тести. Це результати попередньої роботи, **не повторний запуск 2026-10-08**. Окремо у новому аналізі позначено post-commit retries і внутрішній `runCatching` як місця подальшої перевірки.

## E06. Backup і route consistency

- [BackupRepositoryImpl](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupRepositoryImpl.kt): `exportBackup`, `previewImport`, `importBackup`, `insertOrReplace`.
- [BackupImportPolicy](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupImportPolicy.kt): перелік/валідація таблиць та значень.
- [BackupUseCases](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/BackupUseCases.kt): domain contracts.
- [Backup confirmation dialog](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt): preview/confirmation уже існує.
- [full_backup_content](../../../app/src/main/res/xml/full_backup_content.xml), [data_extraction_rules](../../../app/src/main/res/xml/data_extraction_rules.xml): виключення DB без повного узгодження preferences.

Висновок про ризик REPLACE спирається на SQL-семантику та існуючі FK; fresh runtime reproduction import тут не проводився. Див. [SQLite conflict behavior](https://www.sqlite.org/lang_conflict.html) і [Android Auto Backup](https://developer.android.com/identity/data/autobackup).

## E07. REST і квести

- [GenerateDailyQuestsUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCase.kt): `systemTemplateType`, scheduled recommendations, replacement existing main quest.
- [DecideTodayWorkoutUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt): Calendar OFF -> REST.

Статичний контрприклад: OFF + існуючий workout Schedule -> REST decision, але quest path ще може використати scheduled exercises. Не вказано, що це було відтворено на телефоні сьогодні.

## E08. Architect і AI

- [ArchitectScreen](../../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt): `AiModulesBlock`, `ArchitectBriefBlock`, disabled cycle correction/tomorrow cards.
- [ArchitectViewModel](../../../app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectViewModel.kt): availability, context, analysis/chat actions, застосування через use case.
- [AiAvailability](../../../app/src/main/java/com/ihor/thesystem/data/remote/ai/AiAvailability.kt): configured/unconfigured/rate-limited/overloaded/malformed.
- [GetAiDashboardDataUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetAiDashboardDataUseCase.kt): local weekly brief, до 3 suggestions, fallback risk text.
- [ValidateDirectivesUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/ValidateDirectivesUseCase.kt): clamps/blocks за matrix і decision context; unknown ID та non-finite cases потребують окремої перевірки.

Публічна перевірка супроводу SDK: [Google deprecated Android SDK](https://github.com/google-gemini/deprecated-generative-ai-android), [Firebase AI Logic production checklist](https://firebase.google.com/docs/ai-logic/production-checklist).

## E09. Health Connect і release-документи

- [Main manifest](../../../app/src/main/AndroidManifest.xml): тільки READ_SLEEP серед health permissions; rationale entry не оголошений у цьому файлі.
- [HealthConnectPermissions](../../../app/src/main/java/com/ihor/thesystem/health/HealthConnectPermissions.kt): SLEEP mapping.
- [HealthConnectSignalsRepositoryImpl](../../../app/src/main/java/com/ihor/thesystem/data/repository_impl/HealthConnectSignalsRepositoryImpl.kt): availability, granted permissions, sleep read/sum.
- [PRIVACY_POLICY](../../../PRIVACY_POLICY.md), [STORE_LISTING](../../../STORE_LISTING.md), [HEALTH_CONNECT_RATIONALE](../../HEALTH_CONNECT_RATIONALE.md): документована поведінка, не доказ опублікованого застосунку.

Потрібні окремі merged-manifest і device checks. Платформні вимоги звірено з [Health Connect setup](https://developer.android.com/health-and-fitness/health-connect/get-started).

## E10. Statistics і метрики

- [GetStatisticsDataUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt): bounded history, weekly summary, progress proofs, local insights.
- [BetaMetricsAggregator](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/BetaMetricsAggregator.kt): workout dates, current schedules, week-to-date, decision/day counts.
- [StatisticsScreen](../../../app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt): маршрути й основні блоки.

Прогрес, workout count, adherence і XP є різними показниками. Показник у UI не доводить історичної коректності кожної агрегації; потрібні fixtures із неповними sets, зміною плану та різними датами.

## E11. Quality і performance

- [CI](../../../.github/workflows/android-ci.yml): compile, tests, guards, lintDebug, bundleRelease, reports.
- [StartupBenchmarks](../../../baselineprofile/src/main/java/com/ihor/thesystem/baselineprofile/StartupBenchmarks.kt), [BenchmarkJourneys](../../../baselineprofile/src/main/java/com/ihor/thesystem/baselineprofile/BenchmarkJourneys.kt), [BaselineProfileGenerator](../../../baselineprofile/src/main/java/com/ihor/thesystem/baselineprofile/BaselineProfileGenerator.kt): наявні файли конфігурації перевірки швидкості.
- [check-doc-only.ps1](../../../scripts/check-doc-only.ps1): дивиться на весь git status `.kt/.kts`, а не лише на поточну сесію.

Наявність workflow не доводить, що remote CI зелений або branch protection увімкнений. Benchmark-файли не є новими вимірюваннями.

## E12. Основні екрани й маршрути

- [StatusScreen](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/StatusScreen.kt): start -> Routes.Cycle.
- [RpgStatusDashboard](../../../app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt): Actions/Status, Today Order, todo/microtask, week preview, RPG blocks.
- [CycleScreen](../../../app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt): editor, workout, parameters/bonuses, empty plan.
- [CalendarScreen](../../../app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarScreen.kt): month grid, selected day, settings.
- [ProfileScreen](../../../app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt): metrics/profile/settings, lazy workout settings host.

Навігаційні та UI-висновки отримано читанням entry points і цільових блоків. Pixel-level відповідність на малих екранах сьогодні не підтверджувалася.

## E13. Advanced-функції

Точкові входи з перевіреної карти для подальшої роботи; їхні повні алгоритми в цій сесії не переаудитовані:

- [Annual generator](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateAnnualProgressionPlanUseCase.kt), [Annual save](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/SaveAnnualProgressionPlanUseCase.kt).
- [Equipment use cases](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/EquipmentProfileUseCases.kt).
- [Daily sync](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/SyncTodayStateUseCase.kt), [day finalization](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeDayUseCase.kt).
- [Progress proofs](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/BuildProgressProofsUseCase.kt).

Рекомендації щодо їхньої пріоритетності є продуктовим висновком, не твердженням, що кожен алгоритм має баг або взагалі не працює.

## E14. Гейміфікація й сумнівні індикатори

- [CalculateMotivationLevelUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateMotivationLevelUseCase.kt): зважені estimated strength/progress/consistency показники, не психологічне вимірювання мотивації.
- [PlayerProgressionPolicy](../../../domain/src/main/java/com/ihor/thesystem/domain/model/PlayerProgressionPolicy.kt): failure threshold, penalty flag, streak reset.
- [CalculateEffectiveWeightUseCase](../../../domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateEffectiveWeightUseCase.kt): penalty formula; у production-пошуку не знайдено викликів.
- [CycleScreen](../../../app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt): `ParametersAndBonusesPanel`, `ParametersPanel`, `BonusesPanel`.
- [WorkoutAnalysisScreen](../../../app/src/main/java/com/ihor/thesystem/feature/architect/ui/WorkoutAnalysisScreen.kt): motivation result presentation.

Висновок «спростити/прибрати з MVP» не є автоматичним дозволом видалити ці файли. Потрібні погодження змісту й перевірка залежностей.

## Публічні джерела порівняння

Твердження про конкретних конкурентів мають прямі посилання поруч із відповідними рядками в [04](04-POSITIONING-AND-GROWTH.md). Використано офіційні сторінки Hevy, Strong, Fitbod, Freeletics, Gentler Streak, Structured, Duolingo, Habitica та developer listing Walking RPG. Не використовувалися їхні рейтинги/кількість завантажень як доказ ефективності чи прогноз нашого попиту.

Технологічні джерела наведені поруч із рекомендаціями в [01](01-STACK.md). Версії взяті з локального build, а не приписані як найновіші з інтернету.

## Перевірка цього документаційного проходу

Виконано 2026-10-08:

- `git status --short`: зафіксовано попередній dirty state; нова робота міститься лише у `docs/product-review/2026-10-08/`.
- `scripts/check-doc-only.cmd`: **exit 1**, оскільки wrapper перевіряє всю робочу копію й бачить попередні P02/P03 `.kt/.kts` зміни. Це не зелений gate; чужі/попередні зміни не скасовувались для його проходження.
- Додаткова перевірка поточного проходу: SHA-256 **472 `.kt/.kts` файлів до і після збігаються**, доданих/видалених source-файлів немає. Build outputs і `.gradle` виключені з переліку.
- Перевірено 105 локальних Markdown-посилань у 7 нових документах: відсутніх цілей немає.
- `git diff --check`: exit 0, лише попередження щодо CRLF у наявних змінах. Нові untracked Markdown окремо перевірено на trailing whitespace і парність code fences.
- Compile, lint, unit/instrumentation tests і benchmark тут **не запускалися**: зміни документаційні. Телефон не використовувався; жодних нових runtime або performance результатів не заявлено.
- Не виконувалися commit, push, merge, видалення функцій чи зміни архітектури. Статуси P01-P28 не редагувалися.
