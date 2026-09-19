# Промпти реалізації THE SYSTEM: LEVEL UP

Підготовлено 2026-09-19 за [PROJECT_AUDIT](PROJECT_AUDIT.md), [DEVELOPMENT_ROADMAP](DEVELOPMENT_ROADMAP.md)
і [картою функцій](docs/architecture/FEATURE_MAP.md). **Це готовий пакет завдань, а не вже виконані виправлення.**

## Мій фідбек

### Загальна оцінка

**Поточна релізна готовність: приблизно 5.5/10.** Я оцінюю не твою ідею чи вкладені зусилля,
а можливість довірити застосунку щоденні реальні записи без супроводу розробника.
За ясністю основної цінності оцінка вища: близько 8/10, за domain-архітектурою близько 7.5/10.
Ці числа суб'єктивні, з аргументами в аудиті; їх не можна трактувати як сертифікацію або прогноз продажів.

Для мене THE SYSTEM є сильнішим як **щоденний тренувальний помічник із RPG-прогресом**, ніж як
«ще один фітнес-трекер з неоновими картками». Найважливіше питання користувача: що доречно зробити
сьогодні, чому саме це, як це виконати і який реальний результат він отримав. У репозиторії
вже є компоненти для відповіді на всі ці питання. Потрібно зробити їхні взаємодії надійними.

### Що вже сильне

**Є власне обличчя.** Темний sci-fi/HUD, ранг, XP і відчуття розвитку можуть добре працювати
для людей, яким подобається така подача. Shared theme tokens і SystemPanel/techSurface дозволяють
підтримувати єдиний стиль. Я не рекомендую відмовлятися від цього заради типового шаблону.

**Є змістовне ядро.** Today decision, cycles, equipment matching, workout logs, progression і
quests не є лише декоративними екранами. Окремий domain-модуль дозволяє вдосконалювати правила
без переписування всього UI. Це хороший фундамент для невеликої команди або solo-розробника.

**Local-first є перевагою.** Основна цінність не повинна зникати через відсутність інтернету,
API-ключа чи грошей на AI-запит. Публічний Gemini зараз вимкнений, але deterministic system
може залишатися корисною. Це допомагає приватності, контролю витрат і швидкості.

**Правило «AI пропонує, система вирішує» правильне.** ValidateDirectivesUseCase дає зрозумілу
точку контролю. Це значно краща основа для довіри, ніж безпосередній запис довільної відповіді
моделі в тренувальний план. Потрібно перевірити edge cases, а не змінювати сам принцип.

**Ти вже інвестував у перевірки.** Є unit tests, architecture/Compose guards, Room schemas,
migration tests, CI та baseline-profile/performance інструменти. Свіжий прогін під час аудиту:
298 passed і 1 skipped. Це реальний актив, хоча його ще потрібно доповнити integration coverage.

### Що найбільше заважає

**Надійність даних зараз важливіша за красу.** Статично простежені paths показали ризик втрати
сетів інших вправ при редагуванні, скидання draft при resume та небезпечну семантику часткового
backup import. Для тренувального щоденника це критично: людина може пробачити повільну анімацію,
але втрачена історія підриває довіру. Тому ці роботи першими в пакеті.

**Перший запуск не повністю дає обіцяну цінність.** Кроки onboarding уже є, але сам вибір циклу
ще не створює готового schedule із вправами. Новій людині не повинно бути потрібно зрозуміти
внутрішній конструктор перш ніж побачити корисне Today Order. У перші хвилини застосунок має
показати, як він допомагає, а не вимагати від користувача спроєктувати собі всю систему.

**Пояснення іноді впевненіші за дані.** Neutral fallback readiness може виглядати як точне
вимірювання, а manual check-in ще не підключений. Потрібно показувати джерело, свіжість і
невизначеність без залякування чи порожнього екрану. Відсоток готовності не є гарантією безпечності.

**Частини core loop можуть суперечити одна одній.** REST і generation MAIN quest, repeated finish,
completed/incomplete sets у progress charts потребують єдиного трактування. Користувач не має
бачити одночасно «відпочивай» і незрозумілий нульовий тренувальний квест.

**Зелений unit suite ще не означає готовий продукт.** Поточні UI tests не доводять правильну
роботу всіх п'яти реальних вкладок у production navigation. Окремо lint падав на локалі.
Історичні traces показують помітний jank, але в аудиті не було нового device benchmark.
Потрібні справжні сценарії: переривання, повернення, повторний finish, import failure, IME і великі шрифти.

**Scope легко розростається швидше за якість.** AI, annual plans, nutrition, calendars, quests
і profile можуть здаватися рівноцінними напрямами. На старті вони такими не є. Не слід зараз
будувати social network, повний calorie tracker, складний backend або новий redesign, поки
одне тренування від першого відкриття до наступного дня не працює бездоганно.

### Новизна й перспективи

RPG-подача та адаптивні тренування самі по собі не нові. Це видно з першоджерел
[Habitica](https://github.com/HabitRPG/habitica), [Fitbod](https://fitbod.me/) і [Hevy](https://www.hevyapp.com/features/),
зіставлених в аудиті 18.09.2026. Це не означає, що «все вже придумано»: відмінність може бути
в поєднанні локальної роботи, зрозумілих рішень, української мови, швидкого logging і впізнаваного HUD.

Наразі я не назвав би THE SYSTEM сильнішим за зрілі продукти за надійністю повсякденного використання.
Але вузька аудиторія може обрати його не за найдовший список функцій, а за відчуття зрозумілого
персонального прогресу. Для цього потрібні правдиві logs і хороші програми, а не тільки додатковий glow.

Користувачів ще не було, тому retention, готовність платити й product-market fit **невідомі**.
Жоден промпт не створить ці докази замість людей. Після технічної стабілізації потрібна невелика
beta з конкретними сценаріями, а потім виправлення за фактами. Рекомендації щодо монетизації
поки залишаються гіпотезами: додаткові програми, глибша аналітика або теми; не плата за безпеку
чи доступ до власної історії.

### Що означатиме суттєвий прогрес

Я очікую найбільший ефект від правильної послідовності:
**збереження даних -> готовий перший план -> чесний Today Order -> швидкий logging ->
перевірені реальні сценарії -> performance/accessibility polish -> beta evidence.**

28 промптів нижче охоплюють усі 12 знахідок і 8 додаткових ризиків аудиту, а також cross-cutting
питання. Це не гарантія «автоматичних 10/10»: нові bugs, людський review тренувального контенту,
права на assets, Play Console і справжня beta лишаються окремими доказовими gates.

## Як користуватися без зайвих токенів

1. Працюй у цьому самому saved project. Обери один prompt, за замовчуванням P01.
2. Перед відправкою обери рекомендовану модель і Reasoning у доступному model selector.
3. Напиши коротко: `Виконай тільки P01 із docs/implementation/prompts/01-safe-history-edits.md.`
4. Файл уже містить entry paths, прив'язку до AUD-ID/roadmap, acceptance і checks. Не вставляй весь аудит.
5. Дочекайся результату й перевір статус у [PROGRESS](docs/implementation/PROGRESS.md). Потім запускай наступний.
6. P06 незалежний: можна спочатку швидко виправити lint. Решта має явно вказані dependencies.
7. Не запускай паралельні правки shared WorkoutViewModel/Room у кількох задачах. Нову задачу використовуй для наступного завершеного scope, а не для втечі від незакритого defect.
8. Загальні правила й evidence format уже збережені в [EXECUTION_RULES](docs/implementation/EXECUTION_RULES.md).

Файл prompt є готовою інструкцією цілком, не лише code block. Агент має прочитати саме його.
Посилання ведуть на конкретні source files; нові файли для майбутніх reports явно позначені як outputs попередніх кроків.
Пакет не змінює твою модель автоматично й не запускає всі задачі від одного відкриття файла.

## Моделі та витрати

- **GPT-5.6 Luna / Medium:** вузькі зрозумілі правки й документація.
- **GPT-5.6 Terra / High:** більшість багатофайлових задач із відомими межами.
- **GPT-5.6 Sol / High:** data integrity, transactions, concurrency, performance investigation і незалежний аудит.
- **GPT-6 Astra:** не потрібна за замовчуванням; резерв для конкретного складного blocker, якщо дешевший шлях уже виявився недостатнім.

Це моя інженерна маршрутизація, не benchmark саме твого repo. Офіційна документація позиціює
Luna для cost-sensitive задач, Terra як баланс ціни/можливостей, Sol для складної професійної роботи.
[Джерело: OpenAI models](https://developers.openai.com/api/docs/models).
Вищий reasoning дає моделі більше простору на обдумування й може збільшити витрати;
я не рекомендую Max для кожної дрібної зміни. [Джерело: reasoning guidance](https://developers.openai.com/api/docs/guides/reasoning).

**Дешевша модель не обов'язково генерує менше токенів.** Важлива повна вартість успішної задачі
разом із повторними спробами. API pricing не є прямим калькулятором твоїх Codex subscription limits.
Точний виграш для твого тарифу тут не обіцяю. Найнадійніша економія: вузький scope, точковий контекст,
чіткий regression test і короткий handoff. Доступність моделей звіряй у своєму selector;
[офіційний довідник моделей](https://learn.chatgpt.com/docs/models) є джерелом актуальних назв.

## Чи потрібен телефон

**Ні, не завжди.** Host JVM tests, logic, compile, lint і docs виконуються без телефона.
**Android** у таблиці означає телефон **або** емулятор для фінального runtime gate.
**Realme** означає фізичний reference device для порівнянних performance вимірювань.
**API/16KB** означає додаткове сумісне середовище: один Realme не покриває всі platform cases.
Пристрій може бути потрібен лише наприкінці задачі. Без нього код/host tests можна завершити,
але ставити загальне done замість needs_device не можна.

Не потрібно тримати Android Studio відкритою для всіх кроків. Агент використовує доступні Gradle/ADB інструменти;
підключення й USB confirmation перевіряються перед device tests. Особисті дані не очищаються заради тесту.

## Покажчик промптів

Перед **кожним повним промптом** у його файлі зазначені точна модель, Reasoning, потреба в телефоні й dependencies.

| ID | Готовий промпт | Модель / Reasoning | Пристрій |
| --- | --- | --- | --- |
| P01 | [Безпечне редагування історії](docs/implementation/prompts/01-safe-history-edits.md) | Sol / High | Android |
| P02 | [Збереження незавершеного тренування](docs/implementation/prompts/02-durable-workout-draft.md) | Sol / High | Android |
| P03 | [Одноразове завершення й надійний звіт](docs/implementation/prompts/03-idempotent-workout-finish.md) | Sol / High | Android |
| P04 | [Безпечний backup і відновлення](docs/implementation/prompts/04-atomic-backup-restore.md) | Sol / High | Android |
| P05 | [Узгоджений запуск, seed і відновлений профіль](docs/implementation/prompts/05-startup-restore-state.md) | Sol / High | Android |
| P06 | [Lint і правильна локалізація форматування](docs/implementation/prompts/06-lint-and-locale.md) | Luna / Medium | Android на фініші |
| P07 | [Готовий перший план після onboarding](docs/implementation/prompts/07-usable-onboarding.md) | Terra / High | Android |
| P08 | [Чесні пояснення Today Order](docs/implementation/prompts/08-honest-today-order.md) | Terra / High | Android на фініші |
| P09 | [Короткий check-in і актуальність сигналів](docs/implementation/prompts/09-checkin-and-reactivity.md) | Terra / High | Android |
| P10 | [Узгоджені REST, квести та нагороди](docs/implementation/prompts/10-rest-quests-and-rewards.md) | Sol / High | Android |
| P11 | [Опівніч, часові пояси та паралельні оновлення](docs/implementation/prompts/11-daily-rollover-concurrency.md) | Sol / High | Android на фініші |
| P12 | [Правдивий прогрес і повнота історії](docs/implementation/prompts/12-truthful-progress-history.md) | Terra / High | Android |
| P13 | [Збереження вибору вправи при поверненні](docs/implementation/prompts/13-picker-save-lifecycle.md) | Luna / Medium | Android |
| P14 | [Атомарне збереження річних планів](docs/implementation/prompts/14-atomic-annual-plans.md) | Terra / High | Android |
| P15 | [Завершена інтеграція Health Connect](docs/implementation/prompts/15-health-connect-complete.md) | Terra / High | Android |
| P16 | [Перевірений AI gatekeeper і корисний локальний режим](docs/implementation/prompts/16-ai-validation-local-mode.md) | Terra / High | Android |
| P17 | [Коректні локальні beta metrics](docs/implementation/prompts/17-beta-metrics-integrity.md) | Terra / High | Android на фініші |
| P18 | [Справжні UI-сценарії всього застосунку](docs/implementation/prompts/18-real-app-ui-journeys.md) | Sol / High | Android |
| P19 | [HUD polish, доступність і зрозумілі тексти](docs/implementation/prompts/19-hud-accessibility-localization.md) | Terra / High | Android |
| P20 | [Вимірювана швидкість запуску й плавність](docs/implementation/prompts/20-measured-performance.md) | Sol / High | Realme |
| P21 | [Зменшення складності без великого переписування](docs/implementation/prompts/21-bounded-architecture-cleanup.md) | Terra / High | Android на фініші |
| P22 | [Надійні quality gates і підтримуваний build](docs/implementation/prompts/22-ci-and-build-tooling.md) | Terra / High | CI emulator |
| P23 | [Приватність, права на assets і чесні обіцянки](docs/implementation/prompts/23-privacy-assets-release-copy.md) | Luna / Medium | Не для документів/аналізу |
| P24 | [Кандидат релізу, Android16 і16KB](docs/implementation/prompts/24-platform-release-candidate.md) | Terra / High | Android + API/16KB |
| P25 | [Зрозумілий план beta для власника](docs/implementation/prompts/25-beta-study-and-owner-checklist.md) | Luna / Medium | Не для документів/аналізу |
| P26 | [Незалежний GO/NO-GO перед beta](docs/implementation/prompts/26-independent-beta-audit.md) | Sol / High | Android |
| P27 | [Робота зі справжнім beta-фідбеком](docs/implementation/prompts/27-beta-feedback-iteration.md) | Terra / Medium | Не для документів/аналізу |
| P28 | [Фінальна готовність до публічного запуску](docs/implementation/prompts/28-public-release-readiness.md) | Sol / High | Android + API/16KB |

## Етапи

- **P01-P17:** цілісність даних, завершення core loop та перевірка ризиків. P06 можна виконати першим.
- **P18-P22:** real UI journeys, доступність, продуктивність, bounded cleanup і CI.
- **P23-P26:** privacy/assets/owner gates, platform candidate, beta план і незалежний GO/NO-GO.
- **P27:** тільки після фактичного тестування людьми; без feedback не вигадувати результати.
- **P28:** public-readiness за актуальними доказами й правилами, не автоматичний publish.

Повнота: [COVERAGE](docs/implementation/COVERAGE.md). Статуси: [PROGRESS](docs/implementation/PROGRESS.md).
Жоден пункт ще не виконано в межах створення цього пакета; існуючий audit не оголошено виправленим.

## Початок

**Модель: GPT-5.6 Sol. Reasoning: High.**
**Телефон: потрібен на етапі Room integration; для початку читання/коду можна без нього.**

```text
Виконай тільки P01 із файла
C:/Users/gesha/AndroidStudioProjects/TheSystem-master/docs/implementation/prompts/01-safe-history-edits.md.
Прочитай повний файл і referenced EXECUTION_RULES, виконай acceptance/tests,
онови тільки його progress/evidence. Не переходь до P02, не коміть, не пуш і не мердж.
```
