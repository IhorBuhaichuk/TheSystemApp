# THE SYSTEM: LEVEL UP - Project Audit

## Висновок

**Рекомендація: NO-GO для зовнішньої beta з реальними тренувальними даними до закриття P0.**
Для внутрішніх перевірок на окремому тестовому профілі проєкт придатний.
Public release також NO-GO: є lint-блокер, незавершена Health Connect інтеграція та неперевірені release gates.

Головна цінність проєкту не в кількості екранів: це потенційно зрозумілий щоденний цикл
«рішення -> дія -> запис -> доказ прогресу -> наступне рішення». Код уже містить більшу частину
цього ланцюжка, але найбільші прогалини саме між його частинами: перше налаштування не дає
готового тренування, draft може загубитися, редагування історії може видалити інші вправи,
а інтерфейс звучить упевненіше, ніж дозволяють фактичні дані readiness.

**Моя загальна інженерно-продуктова оцінка поточної готовності: приблизно 5.5/10.**
Основа для подальшої розробки значно сильніша, ніж ця оцінка релізної готовності.
Це експертний орієнтир, не арифметичний середній бал, сертифікація чи прогноз успіху.
Надійність даних має право «вето»: гарний UI не компенсує втрату workout history.
Потенціал високий за умови фокусу; product-market fit наразі **невідомий**, бо користувачів ще не було.

## Метод і межі

- Аудит виконано 2026-09-18/19, зовнішні джерела перевірено 2026-09-18.
- Кодова ревізія: `184c0044718e22b97d01cee20f0da707a355c8bc`, branch `codex/overnight-premium-hud-polish`.
- Робоче дерево на старті було чистим. У цьому аудиті змінюються тільки Markdown-документи.
- Прочитано graph/rules/playbooks/product docs; простежено критичні use case -> repository -> DAO -> VM/UI потоки.
- Перевірено Room schema51, startup/seed, navigation, CI/build, tests, backup, HC та AI gates.
- **Код** означає підтверджений статичний шлях; **виконано** означає реально запущену перевірку;
  **ризик** означає обґрунтовану гіпотезу, що ще потребує regression test або device reproduction.
- Під час перевірки `adb devices -l` не показав пристроїв. Нових UI screenshots, TalkBack,
  connected migration tests, first-install benchmark і runtime HC перевірок немає.
- Даних про retention, готовність платити, Play Console account/policies approval немає.
- Це не медичний, юридичний або penetration-test висновок. Алгоритми readiness не валідувалися клінічно.

Навігація: [коротка карта](GRAPH_REPORT.md), [feature/data map](docs/architecture/FEATURE_MAP.md),
[план реалізації](DEVELOPMENT_ROADMAP.md). Лінії коду нижче прив'язані до вказаної ревізії.

## Пріоритетні знахідки

P0 тут означає блокер передачі користувачам даних для зберігання, а не чинний production incident.
P1 потрібно закрити перед beta/public gate відповідної функції; P2 можна планувати після стабілізації.

### AUD-01 / P0: редагування вправи видаляє сети інших вправ

**Підтверджено кодом.** `LogWorkoutSetsUseCase.replaceExistingLog` обирає session за першою
вправою за день, змінює metadata цілого session, викликає `deleteSetsBySession`, а потім вставляє
тільки передані сети однієї вправи. Інший write path у `ProgressionMatrixRepositoryImpl`
робить аналогічне, додатково використовуючи parent INSERT OR REPLACE з CASCADE FK.

Докази: [LogWorkoutSetsUseCase.kt:46](domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt#L46),
[видалення:95](domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt#L95),
[ProgressionMatrixRepositoryImpl.kt:131](app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt#L131),
[WorkoutAnalyticsDao.kt](app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt).
WorkoutViewModel.onLogSetsConfirmed -> StatisticsUseCases -> matrix repository є підключеним шляхом;
StatisticsViewModel також містить альтернативний LogWorkoutSets handler. Не вважати це лише legacy-кодом.

Сценарій regression: session містить squat і press; редагування press має залишити squat,
session timestamp/questId/cycleDay та перерахувати загальний tonnage, а не замінити весь session.
Потрібен явний sessionId, exercise-scoped update/delete, transaction і **реальний Room test**.
Існуючий LogWorkoutSetsUseCaseTest не захищає multi-exercise session повністю.

### AUD-02 / P0: resume очищає незбережений workout draft

**Підтверджено кодом, device reproduction не виконано.** Sets зберігаються в `_userEdits` до finish.
`refreshForCurrentDay()` безумовно очищає map, а CycleScreen викликає його з `RefreshOnResume`.
Повернення після дзвінка, іншого застосунку або системного діалогу може скинути введені дані навіть того самого дня.
Process death також не має durable draft recovery.

Докази: [WorkoutViewModel.kt:293](app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt#L293),
[refresh:434](app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt#L434),
[CycleScreen.kt:110](app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt#L110),
[RefreshOnResume.kt](app/src/main/java/com/ihor/thesystem/core/ui/RefreshOnResume.kt).

Потрібно відокремити refresh даних від destructive reset, прив'язати draft до session/day,
зберігати draft окремо від завершених logs і не давати XP за draft. SavedStateHandle може допомогти
з короткоживучим UI state, але не замінює durable збереження тренування.

### AUD-03 / P1: finish не має стійкого захисту від повтору

**Підтверджена відсутність session idempotency; наслідок є відтворюваним кандидатом.**
`onFinishWorkout` створює новий WorkoutSession без стабільного operation/session ID, без `isFinishing`
gate і запускає coroutine. FinalizeSession спочатку commit-ить logs/XP, а потім будує report та
зберігає directives. Error другої фази може бути показаний уже після успішного запису першої.
Повтор запиту може створити ще один log; приховування діалогу не є storage-level захистом.

Докази: [WorkoutViewModel.kt:315](app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt#L315),
[FinalizeSessionUseCase.kt:47](domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt#L47),
[друга фаза:193](domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt#L193).
**Не стверджую подвійний XP для того самого quest:** CompleteQuest перевіряє ACTIVE у транзакції.
Session uniqueness і quest reward uniqueness є різними інваріантами.

Завершення має повертати committed session навіть коли enrichment/report не вдався; retry за тим самим ID
повинен повернути попередній результат. Тестувати подвійний event, retry і crash між фазами.

### AUD-04 / P0: успішний backup import може бути destructive

**Підтверджено кодом.** Preview/confirmation, whitelist таблиць, bind validation і transaction уже є.
Проте validation допускає subset таблиць, не перевіряє сумісність `appDatabaseVersion` та повноту
графа. INSERT OR REPLACE для parent session може видалити дочірні сети через CASCADE, якщо payload
не містить їх для відновлення. Transaction рятує від SQL failure, але не від логічно неправильного success.

Докази: [BackupUseCases.kt:42](domain/src/main/java/com/ihor/thesystem/domain/usecase/BackupUseCases.kt#L42),
[BackupImportPolicy.kt](app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupImportPolicy.kt),
[BackupRepositoryImpl.kt:58](app/src/main/java/com/ihor/thesystem/data/repository_impl/BackupRepositoryImpl.kt#L58).
Export читає таблиці послідовно без єдиного snapshot transaction; last-export timestamp оновлюється
раніше, ніж підтверджено запис у вибраний користувачем SAF destination.

Окрема проблема: Android backup виключає DB, але залишає preferences, зокрема onboarding flag.
Після prefs-only restore маршрут може пропустити onboarding за порожньої Room DB. JSON переносить
таблиці, але не узгоджує ці prefs. Докази: [backup XML](app/src/main/res/xml/full_backup_content.xml),
[data extraction XML](app/src/main/res/xml/data_extraction_rules.xml),
[OnboardingRepositoryImpl.kt](app/src/main/java/com/ihor/thesystem/data/repository_impl/OnboardingRepositoryImpl.kt),
[route rule](domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt#L29).

Спочатку визначити контракт: повне відновлення із явним підтвердженням або справжній merge.
Не називати generic REPLACE безпечним merge. Додати snapshot export, size/schema/domain validation,
preview conflicts, non-destructive rollback tests, startup-state reconciliation і чесний timestamp.
Перенесення аватара/URI також перевірити окремо: URI не гарантує доступу на іншому пристрої.

### AUD-05 / P1: onboarding завершується без готового плану

**Підтверджено кодом.** CompleteOnboarding зберігає player, equipment, system config і flag,
але не створює schedule/exercise assignments. DatabasePopulator додає singleton rows та каталог вправ,
не програму. Вибір cycle preset задає довжину циклу, а не готове тренування.
Без ручного редактора новий користувач може отримати REST замість першої корисної дії.

Докази: [OnboardingUseCases.kt:38](domain/src/main/java/com/ihor/thesystem/domain/usecase/OnboardingUseCases.kt#L38),
[DatabasePopulator.kt:153](app/src/main/java/com/ihor/thesystem/data/local/room/database/DatabasePopulator.kt#L153),
[DecideTodayWorkoutUseCase.kt](domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt).
Ціль/досвід перетворюються на targets/level, але не зберігаються як повний окремий intent profile.

Створити мінімальну перевірену starter program у domain/setup flow, з equipment matching,
rest days і можливістю редагування. Completion означає не тільки «натиснули кнопку», а готовність
усіх необхідних даних. Не додавати складний генеративний планувальник заради цього.

### AUD-06 / P1: недостатні дані readiness маскуються впевненим поясненням

**Підтверджено статичним пошуком write paths і mapper.** Є ReadinessRepository/DAO та алгоритм,
але немає підключеного daily check-in UI, що записує readiness entry. За відсутності даних
використовується ReadinessInput за замовчуванням, нейтральний score або sleep context з HC.
Domain додає warnings про fallback/stale data; TodayOrderUiMapper їх не показує й може писати
«Перешкод для тренування немає» з точним відсотком.

Докази: [DecideTodayWorkoutUseCase.kt:254](domain/src/main/java/com/ihor/thesystem/domain/usecase/DecideTodayWorkoutUseCase.kt#L254),
[TodayOrderUiMapper.kt](app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapper.kt),
[CalculateReadinessUseCase.kt](domain/src/main/java/com/ihor/thesystem/domain/usecase/CalculateReadinessUseCase.kt).
Nutrition має аналогічний розрив: SaveNutritionEntryUseCase існує, feature caller не знайдений.

Показувати provenance, freshness і «даних недостатньо» без порожнього стану; не ототожнювати
heuristic score з виміряною безпекою. Додати короткий необов'язковий check-in для реально використаних
signals. Nutrition не розширювати в calorie tracker до підтвердження потреби.

### AUD-07 / P1: REST і генерація main quest не узгоджені

**Підтверджено статично.** Calendar OFF повертає REST. `systemTemplateType()` для REST повертає null,
після чого GenerateDailyQuests бере вправи існуючого workout schedule. AdjustWorkoutRecommendation
правильно зануляє weight/sets/reps, але список recommendations лишається непорожнім; умова створення
MAIN перевіряє список, а не REST/можливість виконання. Repository вставляє нульові targets.

Докази: [GenerateDailyQuestsUseCase.kt:58](domain/src/main/java/com/ihor/thesystem/domain/usecase/GenerateDailyQuestsUseCase.kt#L58),
[AdjustWorkoutRecommendationUseCase.kt](domain/src/main/java/com/ihor/thesystem/domain/usecase/AdjustWorkoutRecommendationUseCase.kt),
[QuestRepositoryImpl.kt:73](app/src/main/java/com/ihor/thesystem/data/repository_impl/QuestRepositoryImpl.kt#L73).
Це доказ phantom workout quest, **не доказ**, що REST автоматично призначає важкі ваги.

Додатковий ризик: GenerateDailyQuests може видаляти MAIN при зміні recommendations без status guard.
Збереження completed quest та відсутність повторних нагород після regeneration потребують тесту.
Єдине рішення має визначати і CTA, і task generation, і missed-day policy, і report.

### AUD-08 / P1: фактичний Android lint-gate падає

**Відтворено виконанням.** `:app:lintDebug`: 1 error, 45 warnings, 1 hint.
Error `NonObservableLocale` у
[TonnageChartCanvas.kt:33](app/src/main/java/com/ihor/thesystem/feature/statistics/ui/components/TonnageChartCanvas.kt#L33):
`Locale.getDefault()` читається у Composable без observable locale state.
Виправлення має використати API локалі, сумісний з установленою Compose-версією; не suppress/baseline.

Більшість warnings стосуються нових dependency versions; це не наказ оновити все одночасно.
Окремо перевірити StringFormatCount, ConfigurationScreenWidthHeight і ModifierParameter.
Unit source guard із назвою ReleaseLint не замінює реальний Android lint.

### AUD-09 / P1: Health Connect privacy rationale не має runtime entry

**Підтверджено manifest і merged manifests debug/release.** READ_SLEEP є, але немає Activity intent
`androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` і Android14+ permission-usage alias.
Markdown rationale не відкривається автоматично з системного permission screen.

Докази: [AndroidManifest.xml](app/src/main/AndroidManifest.xml),
[HealthConnectPermissions.kt](app/src/main/java/com/ihor/thesystem/health/HealthConnectPermissions.kt),
[HEALTH_CONNECT_RATIONALE.md](docs/HEALTH_CONNECT_RATIONALE.md).
Додати native policy screen/entry, перевірити HC package visibility на старіших supported Android,
grant/deny/revoke/provider-missing і fallback. Вимога описана в [офіційному HC setup](https://developer.android.com/health-and-fitness/health-connect/get-started).

### AUD-10 / P1: історія ваг може врахувати незавершений сет

**Підтверджено query path; потрібен integration regression.** FinalizeSession отримує всі valid inputs,
включно з `isCompleted=false`. Annual/history SQL обчислює MAX(weight) без completed filter.
Невиконана запланована вага може виглядати як реальний результат. Це окремо від session tonnage,
який WorkoutViewModel рахує тільки за completed sets.

Докази: [WorkoutViewModel.kt:326](app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt#L326),
[WorkoutAnalyticsDao.kt:86](app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt#L86).
Сценарій: виконано 40 кг, 100 кг залишено incomplete; доказ прогресу має показати 40, не 100.
Уточнити один контракт для completed work, warmup, bodyweight, time та distance перед зміною SQL.

### AUD-11 / P1: тести не покривають головний end-to-end сценарій

**Підтверджено тестовими файлами.** ResponsiveLayoutTest перевіряє standalone Status, scrolling,
його внутрішній horizontal mode, test-shell bottom tabs та representative text components.
Він не запускає всі п'ять реальних screens у production AppNavGraph. `assertIsDisplayed` не
доводить, що glyphs не обрізані, а зовнішній navigation swipe не конфліктує з внутрішнім.

Докази: [ResponsiveLayoutTest.kt](app/src/androidTest/java/com/ihor/thesystem/ui/ResponsiveLayoutTest.kt),
[AppNavGraph.kt](app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt),
[CI workflow](.github/workflows/android-ci.yml).
Connected migration/UI tests не виконуються в поточному CI. У Unit suite багато корисних guards,
але regex перевірка наявності transaction не доводить коректність її семантики.
Повторні filtered запуски того самого test task у CI також можуть перезаписати повний XML/report
результат останнім subset. Зберігати повний suite evidence до filtered runs або прибрати зайві повтори;
за lint failure release step пропускається, тому always-upload не гарантує наявності AAB.

### AUD-12 / P2: beta metrics не є історичною аналітикою продукту

**Підтверджено алгоритмом.** Weekly planned/missed обчислюється за поточним schedulesByCycleDay,
а факт workout зводиться до дати будь-якого log. Зміна schedule може переінтерпретувати минуле;
manual log не обов'язково означає виконання саме запланованого Today Order. First-workout і onboarding
є booleans, не timestamps для time-to-value. Local counters самі по собі не дають cohort retention.

Докази: [BetaMetricsAggregator.kt](domain/src/main/java/com/ihor/thesystem/domain/usecase/BetaMetricsAggregator.kt),
[GetBetaMetricsUseCase.kt](domain/src/main/java/com/ihor/thesystem/domain/usecase/GetBetaMetricsUseCase.kt).
Для малої beta достатньо добровільного знеособленого summary export і коротких інтерв'ю.
Не впроваджувати analytics SDK лише для красивого dashboard.

## Ризики, які ще треба довести

| Ризик | Чому підозрюється | Точна наступна перевірка |
| --- | --- | --- |
| Worker/foreground double rollover | guards читаються до transaction у SyncTodayState/FinalizeDay | concurrent invocations на одному clock/db, рівно один advance/reward |
| Seed/onboarding race | async DatabaseModule population та окремий setup flow | slow/failing seed + fast completion + process kill; жодного default overwrite |
| ExercisePicker cancellation | add coroutine та immediate popBackStack, VM scope | delay repository write, select/pop, перевірити збереження і lifetime VM |
| HC sleep double count/window | readRecords + sum, кілька джерел/overnight records | overlap, midnight, timezone, pagination fixtures; порівняння з HC aggregate semantics |
| Stale Today після HC/check-in | не кожен input є reactive trigger status flow | change signal без restart, перевірити всі dependent screens |
| Annual plan save partial | SaveAnnualProgressionPlan пише вправи послідовно | failure на другій вправі; transactional plan consistency |
| Ліміти history | getAllSessionLogs LIMIT100, weekly LIMIT200 | висока щільність sessions; query назвами/контрактами не обіцяє all-time |
| Avatar/asset portability/licensing | URI, зовнішні images, стилізація під anime | inventory source/license/offline behavior; не припускати порушень без evidence |

Ці рядки не є оголошенням підтверджених runtime bugs. Їх потрібно перевести у test cases перед refactor.

## Сильні сторони

1. **Domain boundary уже існує.** Pure Kotlin models/use cases/contracts відділені від Android.
   Це дозволяє виправляти рішення без переписування UI або зміни всієї архітектури.
2. **Local-first core.** Release не вимагає Gemini або акаунта; можна довести цінність без backend витрат.
3. **AI gatekeeping.** ValidateDirectivesUseCase використовується у apply/finalize paths;
   AI не є прямим автором довільних mutations. Не руйнувати цю межу.
4. **Гейміфікація пов'язана з дією.** XP/quests/ranks спираються на logs і progression,
   а не тільки на довільне натискання «я молодець». Це хороша база за умови правильної idempotency.
5. **Shared visual system.** SystemTheme/SystemPanel/techSurface дають шлях до консистентності
   через невеликий diff, без індивідуальної стилізації десятків екранів.
6. **Є інженерні запобіжники.** CI, schema export, migration tests, unit guards, clock abstraction,
   versioned metadata seeding, macrobenchmarks. Для solo-проєкту це змістовний фундамент.
7. **Попередні performance зміни ґрунтувалися на traces.** Є задокументовані невдалі експерименти,
   а не припущення, що менше Composable або більше кешів автоматично робить швидше.

## Оцінки за напрямами

Шкала: 1-3 = фундаментальні прогалини; 4-6 = працюючі частини, але beta gaps;
7-8 = послідовна перевірена реалізація; 9 = висока якість з польовими доказами;
10 = ціль без відомих значущих прогалин у визначеному scope, не «ідеально назавжди».

| Напрям | Бал / 10 | Впевненість | Що найбільше стримує |
| --- | --- | --- | --- |
| Чіткість цінності | 8 | середня | Обіцянка daily decision ще не підтверджена користувачами |
| Замкненість core loop | 5 | висока | starter schedule, drafts, consistent Today decision |
| Onboarding/time-to-value | 4 | висока | Налаштування не веде до готового першого workout |
| Workout logging | 4 | висока | Дані можуть зникнути при resume/edit; finish retry |
| Domain architecture | 7.5 | висока | Локальні хороші межі, але orchestration/invariants не всюди атомарні |
| Persistence/restore | 4 | висока | REPLACE, parallel write paths, prefs/DB lifecycle mismatch |
| Readiness correctness/explainability | 4.5 | середня | Неявні defaults, missing check-in, не доведена ефективність правил |
| RPG/progression | 7 | середня | Потрібні completed/regeneration/time invariants, поведінкові тести |
| No-AI usefulness | 7 | середня | Core живий; зайві disabled modules і неповний setup заважають |
| AI safety boundaries | 7 | середня | Gatekeeper є; потрібні adversarial/context-failure fixtures |
| Visual system consistency | 7 | середня | Tokens/shared primitives є; поточний rendered UI не перевірявся |
| Accessibility/responsiveness | 5 | низька | Немає свіжого TalkBack/5-tab/large-font proof |
| Performance | 5.5 | низька | Cold path покращений історично, tab jank залишається |
| Tests/CI | 6.5 | висока | Unit coverage корисний, але lint red та бракує integration journeys |
| Privacy/security posture | 6 | середня | Мало permissions/no release key; backup/data disclosures потребують узгодження |
| Store/release readiness | 4 | висока для repo | HC rationale, signing/Console/runtime proofs відсутні |
| Localization/content clarity | 5.5 | середня | Hardcoded copy, locale lint, метафори іноді випереджають ясність |
| Novelty of concept | 5.5 | середня | RPG habits/adaptive workouts уже існують; комбінація може відрізнятись |
| Competitive execution now | 4.5 | середня | Надійність базового logging важливіша за кількість модулів |
| Retention/market fit/revenue | Не оцінено | немає даних | Немає реальних cohort/payment experiments |

Ці бали не означають, що UI поганий або проєкт треба переписувати. Найдешевший шлях до значного
покращення оцінки: закрити кілька небезпечних зв'язків, а не додати ще одну велику feature.

## Продукт, новизна і конкуренція

Позиціювання варто звузити: **персональний тренувальний командний центр для людей, яким
подобається RPG-прогрес і потрібна одна зрозуміла дія сьогодні, з роботою offline**.
«Life operating system для всіх» зараз розпорошить розробку й ускладнить onboarding.
Початкова аудиторія як гіпотеза: дорослі новачки/ті, хто повертається до силових тренувань,
люблять anime/HUD і не хочуть щодня конструювати програму. Не починати з реабілітації або клінічних обіцянок.

| Продукт / джерело | Вже вирішує | Висновок для THE SYSTEM |
| --- | --- | --- |
| [Hevy features](https://www.hevyapp.com/features/) | Logging, routines, progress, social | Logging має бути не повільнішим і не менш надійним; не змагатися кількістю social features |
| [Fitbod](https://fitbod.me/) | Personalized workouts з equipment/goals/history/recovery | «Адаптивність» сама по собі не унікальна; потрібне зрозуміле пояснення і довіра |
| [Freeletics Adapt Session](https://help.freeletics.com/hc/en-us/articles/360003933780-Adapt-your-Bodyweight-training-session) | Підлаштування тренування до обставин | Пропуск/брак часу мають вести до доречної альтернативи, не до сорому |
| [Habitica official repository](https://github.com/HabitRPG/habitica) | Goals/habits як RPG | XP і quests не є новизною; відмінність у зв'язку з перевіреним workout history |

Це зіставлення заявлених функцій за першоджерелами, не лабораторний тест конкурентів.
Не перевірялися їхні алгоритми, retention чи поточні локальні ціни.

Перспективна комбінація: localized native HUD + offline daily decision + прозорий proof of progress.
Це **гіпотеза відмінності**, не доведена унікальність або moat. Довгостроковий захист може з'явитися
через довіру, якість тренувальних шаблонів, зрозумілу історію рішень і аудиторію, а не через сам glow.

Головні слабкі місця продукту: завеликий видимий scope для першого використання, неоднозначні
назви/терміни, зайва відстань від Today CTA до запису, відсутність реальних feedback loops,
можлива підміна прогресу красивими XP без достовірних logs. Рішення: «одне тренування від початку
до кінця без втрат» перед «п'ять вкладок виглядають ідеально».

## UX, естетика і поведінка

Зберегти темний метал/тоноване скло, округлення великих панелей, контрольовані cyan/violet accents,
узгоджене освітлення й typography hierarchy. Не вводити новий дизайн-фреймворк або web UI.
HUD має підтримувати читання й logging; анімація не повинна затримувати приймання вводу.

Наступні перевірки: 360x640 dp і звичайний телефон, font scale1.0/1.3/2.0, IME, TalkBack,
gesture/3-button navigation, reduced/system-disabled animation, contrast disabled/secondary labels,
не лише колір для completed/error. Мінімальний target для touch controls і semantics спираються на
[Compose accessibility guidance](https://developer.android.com/develop/ui/compose/accessibility/api-defaults).
Це quality targets; не оголошення, що поточні екрани вже пройшли їх.

Замість прихованих swipe-only функцій важливі дії мають лишатися доступними натисканням.
Для нового користувача показати зрозумілий next action, для досвідченого зберегти швидкий editing path.
Прогрес за відновлення/повернення має підтримувати регулярність без примусу і небезпечних streak incentives.

## Performance і maintainability

За [історичним checkpoint 2026-07-27](docs/PERFORMANCE_OPTIMIZATION_STATUS.md), Realme RMX3710/API35/90Hz:
cold median936.5 ms без profile і952.5 ms з profile; Statistics після optimization p50/p90 =61/117 ms,
jank40%; Profile =27/73 ms, jank15.38%. Це **старі вимірювання**, не новий результат цього аудиту.
Profile не показав достовірного cold-start виграшу; він допоміг іншим journeys.
5 iterations, non-minified release і OEM gfxinfo обмежують переносимість висновків.
Числа кадрів не порівнювати з часом відкриття екрана чи колишніми5.3s без однакового протоколу.

Наступна оптимізація: frozen workload, однаковий device/build/refresh rate, traces перед зміною,
розділити first install/cold/warm та time-to-usable-Today, не приховувати роботу за довшим splash.
Не запускати macrobenchmark на єдиній копії важливих даних: він може перевстановлювати пакет.

WorkoutViewModel об'єднує logging, schedule, equipment, backup і settings; великі dashboard files
підвищують вартість перевірок. Розділяти за відповідальністю після regression tests, без масового
переміщення файлів. Не дробити на десятки Gradle modules завчасно. Domain tests можна згодом
перенести в domain для швидшого вузького циклу, залишивши app guards там, де потрібен Android source.
AGP deprecated flags/Baseline Profile plugin compatibility warning потребують окремого build-task,
не dependency-update шторму одночасно з виправленням даних.

## Стандарти й release стан

| Вимога / рекомендація станом на перевірку | Поточний стан і наступний крок |
| --- | --- |
| [Google Play target API](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) | Для new apps/updates з31.08.2026 потрібен API36+; target36 уже відповідає. Це не доводить runtime compatibility |
| [Android16 behavior changes](https://developer.android.com/about/versions/16/behavior-changes-16) | Перевірити edge-to-edge, back та adaptive windows на API36; не орієнтуватися лише на Realme/API35 |
| [Health Connect setup](https://developer.android.com/health-and-fitness/health-connect/get-started) | READ_SLEEP мінімальний; rationale Activity/alias і permission lifecycle ще не доведені |
| [Health apps policy](https://support.google.com/googleplay/android-developer/answer/14738291?hl=en) | Потрібні коректна декларація та публічна policy, без medical claims; Console стан не перевірений |
| [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) | Local-only processing не тотожне off-device collection; перевірити фактичні SDK/transfers, не переносити список Room полів механічно в форму |
| [Android Auto Backup](https://developer.android.com/identity/data/autobackup) | DB exclusions є, preferences за замовчуванням лишаються; узгодити policy та відновлення |
| [16KB support](https://developer.android.com/guide/practices/page-sizes) | AAB містить libandroidx.graphics.path.so для4 ABI; потрібен ELF/APK alignment та runtime test, Kotlin-only exemption непридатний |
| [New personal account testing](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) | Якщо account створено після13.11.2023:12 opted-in testers безперервно14 днів перед production application; тип/дата account невідомі |

На перевіреній сторінці16KB вказано enforcement для updates з01.02.2027; це не привід відкладати
compatibility для нового застосунку. Перед submission повторно звірити Console й офіційні правила.
Мала discovery-група5-10 людей із PRODUCT_STRATEGY не замінює обов'язкового closed test, якщо він застосовний.
Виконання тестового мінімуму також не гарантує автоматичне production approval.

Privacy/store docs уже уникають diagnosis/treatment promises, але ручний readiness fallback описаний
краще, ніж реалізований. Потрібні hosted policy URL, відповідальна особа/контакт, узгодженість з
реальним backup/AI behavior, актуальні screenshots і перевірка прав на assets. Repo Markdown сам
по собі не доводить опубліковану policy чи завершену Play Console конфігурацію.

## Стратегія, метрики й монетизація

Перший milestone: нова людина без допомоги створює доречний план, розуміє Today Order,
записує workout, бачить чесний proof, повертається до зрозумілої наступної дії.
North-star hypothesis: кількість користувачів на тиждень, які завершили доречну для себе заплановану дію
і повернулися в інший день. Не оптимізувати просто кількість workouts: rest/recovery теж можуть бути успіхом.

Вимірювати funnel: setup started/completed -> actionable order -> first started/completed workout
-> друга активна дата -> W2/W4 return. Розділяти «відкрив», «побачив рішення», «виконав».
Додатково: time-to-first-value, manual corrections, draft recovery, import success, частка unknown-data
decisions, crash-free sessions і task success у usability test. Точні визначення й gates є в roadmap.

Гіпотеза монетизації після retention: безкоштовний надійний core logging/локальний план/базовий backup;
оплата за додаткові програми, поглиблений trend analysis або косметичні теми. Не paywall-ити
безпеку, доступ до власних даних чи виправлення неправильної рекомендації. AI може бути optional paid
extension лише після обґрунтування вартості й privacy, не головною причиною оформити підписку.
Підписка потребує повторюваної цінності; для статичних тем доречніше одноразове придбання.
Рекламу та продаж health data не рекомендую для цього positioning.

Прогноз доходу без active users, conversion/churn, ціни та acquisition cost був би вигадкою.
Спочатку перевірити willingness-to-pay інтерв'ю/пропозицією, потім модель unit economics.
Revenue potential не оцінювати кількістю Kotlin classes або красою screenshots.

## Що не робити зараз

- Не починати новий великий redesign, social feed, marketplace, wearables, cloud sync або власний backend.
- Не додавати AGSL/Lottie/важкі ефекти до стабільного performance baseline.
- Не розширювати Health Connect permissions без конкретного споживача в core loop.
- Не маскувати lint помилку suppression або backup ризик написом «усе локально».
- Не робити destructive migration через відсутність публічних користувачів без окремого рішення власника.
- Не витрачати тижні на сумісність із неіснуючими старими production releases; зберегти developer data
  та визначений pre-release schema floor, доки явно не погоджено інше.
- Не оголошувати 10/10 до польових перевірок і не доручати AI клінічну валідацію алгоритмів.

## Виконані перевірки

| Перевірка | Результат |
| --- | --- |
| `scripts/check-tests.cmd` | PASS; первинний запуск був UP-TO-DATE |
| `:app:testDebugUnitTest --rerun` | Fresh run:91 suites,299 tests;298 passed,1 skipped,0 failures/errors |
| `:app:lintDebug` | FAIL: NonObservableLocale,1 error/45 warnings/1 hint |
| `:app:bundleRelease` | PASS окремим запуском після lint failure; 19.09 підтверджено task gate |
| `:app:compileDebugAndroidTestKotlin` | PASS; це compile, не виконання connected tests |
| Release BuildConfig | Gemini key порожній; client AI=false |
| AAB signature/archive | META-INF signing entries відсутні; artifact unsigned, містить native path library |
| Device execution | NOT RUN, adb devices порожній під час перевірки |
| `scripts/check-web-ui-guard.cmd` | PASS окремим запуском 19.09 |
| `scripts/check-doc-only.cmd` | PASS; Kotlin/KTS changes відсутні |
| Documentation | Внутрішні посилання й точні file paths перевірені; `git diff --check` без помилок |

AAB: `app/build/outputs/bundle/release/app-release.aab`, 9,873,331 bytes.
SHA-256: `6A8103362874120158FFDBA7EEB7E619CA6F5586768B843DD918957FB7694E4A`.
Артефакт є build proof цього checkout, не схваленим beta candidate.
Перший combined Gradle invocation завершився FAIL через lint, тому bundle перевірявся окремо;
успішний `lintVitalRelease` не замінює повний lintDebug.
Unit suite включав Room/schema та Compose-only guards. Connected Room tests, 16KB runtime,
TalkBack, current performance та actual five-tab journey залишаються незапущеними.
Після повного suite був окремий filtered web-UI guard run; локальна test-results директорія
може містити саме останній subset, а наведені299 tests належать попередньому fresh full run.

## Рішення

Продовжувати розробку, але змінити порядок: **цілісність даних -> завершений перший цикл ->
чесні рішення -> beta-докази -> performance/UI polish -> масштабування**.
Найближчі зміни й критерії завершення наведено в [DEVELOPMENT_ROADMAP](DEVELOPMENT_ROADMAP.md).
Після виправлень потрібен короткий повторний audit конкретних AUD-ID, а не повне повторне сканування.
