# R02. Сценарії та карта дій

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний для contracts; фактичні спостереження UI лише з наявним screenshot/device.**
**Залежності: R01.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [x] Scope/prerequisites і потрібні approvals підтверджені.
- [x] Завдання реалізовано в межах цього prompt.
- [x] Host-перевірки пройдені; результати записані.
- [x] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [x] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

Завершено 2026-10-12: [UX_CONTRACT](../../design/UX_CONTRACT.md) та
[ACTION_INVENTORY](../../design/ACTION_INVENTORY.md), 130 action/contract groups, 12 routes.
Це static docs-only task; runtime не застосовується. H01-H06 є proposals, не внесеними
layout/route-name змінами; конкретний hierarchy/frame approval потрібний до R04.
G01-G06 відкриті у своїх implementation owners; [evidence](../evidence/R02.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R02**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/remaster/FEATURE_SCOPE.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/architecture/FEATURE_MAP.md](../../architecture/FEATURE_MAP.md)
- [docs/design/DESIGN_WORKFLOW.md](../../design/DESIGN_WORKFLOW.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/core/navigation/Routes.kt`
- `app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/StatusScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/WorkoutDialogHost.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Створи NEW docs/design/UX_CONTRACT.md: first install -> usable plan -> Today -> workout -> finish -> next day; повернення після перерви; backup; без permissions; offline.
2. Створи NEW docs/design/ACTION_INVENTORY.md: дія/route/event/owner/state/source file для всіх 5 вкладок, onboarding, dialogs, picker, Architect. Розширюй читання тільки на screen з FEATURE_MAP і його event contract.
3. Запиши load/empty/error/unknown/saving/saved/restore/cancel states. Розділи існуюче і planned P07-P17; не вдавай майбутній контракт уже реалізованим.
4. Запропонуй plain-language hierarchy без автоматичного перейменування routes. Кожну дію map keep/move/merge/hide тільки за R01. Це safety checklist для R08-R14.

### Приймання

- Кожен існуючий logging/navigation/backup action має місце в новому journey.
- Для Today/report зазначені що/чому/дія/результат і incomplete data.
- UX layout/назви, що змінюються, мають owner approval до R04; код незмінний.

### Перевірка

Docs-only; targeted symbol/path existence. Не називати статичне читання тестом користування.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R02 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R02.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

