# R10. Calendar: план і факти без плутанини

**Модель: GPT-6 Luna; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Для acceptance потрібен device/emulator.**
**Залежності: R07, P10, P11, P12.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R10**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/calendar/ui/CalendarSettingsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/calendar/viewmodel/CalendarViewModel.kt`
- `app/src/main/java/com/ihor/thesystem/feature/calendar/viewmodel/CalendarSettingsViewModel.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Реалізуй approved Calendar nodes з shared components; збережи date/month navigation, settings, todos та day actions.
2. План, OFF/recovery, completed/missed і unknown не позначай однаково. UI не перераховує day rules.
3. Перевір довгі українські назви/місяці, порожню дату, dense events і повернення зі settings. Date selection не губиться без потреби.
4. CalendarSettings підключай лише за його disposition у FIGMA_MAP: `new-approved` реалізувати за node, `preserve-current` лишити без redesign і перевірити shared-token regression. `pending` повернути в R06, не домальовувати довільно.

### Приймання

- Критичний текст видимий, grid не роз'їжджається, всі day actions reachable.
- Реальний REST/planned/completed state узгоджений зі Status.
- Domain і persistence незмінні; mapper change тільки з regression test.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Calendar використовує outcome P10/P12 та effective-plan identities: planned, partial, completed і off не один маркер. Клік дати переносить explicit date до plan/history; resume не викликає прихований selectToday.
- Після finish/history edit/todo write/global plan edit потрібні актуальні проєкції без restart. Перевір одну дату через Status -> Calendar -> Тренування -> Back.

### Перевірка

check-quick; check-web-ui-guard; CalendarSettingsViewModel tests; focused Calendar connected flow + screenshots.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R10 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R10.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

