# R06. Решта вкладок і системні стани у Figma

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний; Figma.**
**Люди: 3-5 добровільних учасників без знайомства з аніме-концептом; організує власник.**
**Залежності: R05.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Ранню UX-перевірку core prototype з реальними учасниками виконано, суттєві перешкоди усунено й повторно перевірено.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R06**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/design/UX_CONTRACT.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Створи editable Calendar/System/Statistics/Profile та Architect states в обраному стилі. Наявний Status перевикористати, не малювати заново.
2. Показати empty/sparse/offline, plan vs history, HC denied, backup preview/confirm/failure. Спочатку ключовий сценарій кожної вкладки, не всі декоративні варіанти.
3. Передбач місце для майбутнього контекстного Pro access без dead premium shell; не додавати trial перед core loop і не заявляти доступний AI.
4. Звір action inventory 1:1, зафіксуй node mapping та owner-approved зміни hierarchy. Не генеруй app код.
5. Для кожного secondary screen/dialog із ACTION_INVENTORY запиши у FIGMA_MAP одне рішення: `new-approved` з node/variants/approval або `preserve-current` з baseline revision, незмінними діями й відповідальним R-task. Обов'язково охопи CalendarSettings, ExercisePicker, WorkoutScheduleSettings, AnnualProgressionPlan/Details/ManualEditor, WorkoutAnalysis і backup/edit dialogs. Не малюй заново те, що погоджено зберегти; не приховуй actions через відсутній макет.
6. На core prototype R05 проведи короткий think-aloud із 3-5 добровільними людьми без аніме-контексту. Власник залучає людей; агент не контактує з ними автоматично. Використай синтетичні дані й нейтральні завдання R05 без підказування потрібного шляху. Запиши спроби, виконання самостійно/з допомогою, нерозуміння слів і наступної дії, а не вигадані UX-оцінки.
7. Створи NEW `docs/design/PROTOTYPE_FEEDBACK.md`: revision, анонімні IDs/згода без персональних даних, завдання, фактичні спостереження, проблеми, рішення й повторна перевірка істотних перешкод. Це рання якісна перевірка, не beta, не доказ retention/market fit. За відсутності учасників статус `needs_owner`; можна завершувати незалежні Figma-частини R06, але не закривати R06 і не запускати R07.
8. Виконай approved [D27 progress placement](../../design/R04_SELECTED_DESIGN.md#progress-placement--d27): інформацію колишнього Status-блоку «Деталі прогресу» розмісти в Statistics, не повертай цей бокс у Status чи workout summary. Серія/рівень/ранг у fixture не означають нові розрахунки або обов'язкову RPG-ієрархію; покажи корисні existing дані зрозуміло й без дублювання. Задокументуй destination для native R12.

### Приймання

- Усі 5 вкладок мають взаємопов'язані navigation links, жодна потрібна дія не загублена.
- Shared components/rounded shape консистентні, critical copy не clipping.
- Стан кожного прийнятого макета/approval записаний у FIGMA_MAP.
- Кожен secondary journey має явний design disposition й owner task; немає немапованих shipped дій.
- Реальні спостереження щонайменше 3 учасників зафіксовані; блокери розуміння/завершення основного сценарію виправлені та повторно перевірені на прототипі. Власник схвалив перехід до R07. AI-review або власний walkthrough не замінює цих людей.

### Перевірка

Figma inspection/screenshots, переходи; docs-only.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R06 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R06.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

