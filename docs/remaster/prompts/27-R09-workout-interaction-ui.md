# R09. Зручний logging без втрати чернетки

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен для IME, process/lifecycle і справжнього finish.**
**Залежності: R08, P01, P02, P03, R27.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R09**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/implementation/evidence/P03.md](../../implementation/evidence/P03.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/status/ui/WorkoutDialogHost.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/workout/ActiveDayCard.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/workout/ActiveDayInputHelpers.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutReportDialog.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutDraftDialogs.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Впровадь approved workout/report view, зберігши weight/bodyweight/time logging, notes/edit/cancel/restore/discard paths.
2. Оптимізуй focus/IME і явне complete set; previous values не стають completed автоматично. Не додавай gesture-only команд.
3. Не змінюй completionId/transaction contract; report негайно підтверджує persisted результат, AI опційний.
4. Для кожної moved action звір inventory. Новий timer не входить у цей prompt.

### Приймання

- Incomplete/completed різняться не лише кольором; quick logging/edit/finish accessible.
- Повторний tap/back/network failure не дублює session; draft restore працює після UI змін.
- Short report показує виконане, реальний proof/невідомість, local verdict і next step.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Прив'яжи PL/L0/L1/F/R/D/A/EW/SE/RD до P02/P03/P10/R27. A/EW Apply/Cancel працюють із session draft, не глобальними equipment setters. Повний UI лише Compose/shared tokens.
- F є явним partial-finish confirmation; Cancel повертає поточний draft без writes; Confirm перевіряє його актуальну revision і використовує existing completionId/once-only commit.
- SE має persistent actionable error state, preserved inputs і retry того самого commit; foreground UiErrorEffect не замінює цей стан. Після commit/report failure показати saved facts, не завершувати знову.
- R primary повертає Status, RD відкриває точний sessionId і повертається у report; ці переходи не запускають rewards. Report відрізняє completed sets від exercises/full plan.
- EW unknown не0/24kg; add/edit/uncheck/technique/menu/restore/discard paths працюють. На draft conflict не видаляти completed sets мовчки.
- Тести включають back/cancel, rapid toggles, зміни після partial-confirm, failure-before/after-commit і restore adjusted draft.

### Перевірка

check-quick; check-tests; check-web-ui-guard; наявні draft/finish connected tests + logging UI journey.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R09 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R09.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

