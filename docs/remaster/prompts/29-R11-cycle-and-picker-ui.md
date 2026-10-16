# R11. System/Cycle і вибір вправ

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Для acceptance потрібен device/emulator.**
**Залежності: R07, R09, P07, P13, P14.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R11**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/exercise_search/ui/ExerciseSearchScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutScheduleSettingsDialog.kt`
- `app/src/main/java/com/ihor/thesystem/feature/architect/ui/AnnualProgressionPlanScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Реалізуй approved cycle/settings/picker presentation, зберігаючи edit/add/remove/select/start callbacks і confirmations.
2. Plain-language core plan на першому рівні, advanced лише за approved scope; не видаляти annual storage.
3. Збережи P13 save lifetime і повернення з picker; selected exercise/equipment labels не обрізати.
4. Сумісність R09 dialog contract перевір, не переписуй паралельно ActiveDayCard.
5. ExercisePicker, schedule dialog та AnnualProgressionPlan: спочатку їхнє рішення R06 у FIGMA_MAP. `new-approved` має node/states; `preserve-current` не потребує нового layout, але вимагає перевірки tokens/actions/back. Без рішення не починати visual migration цих surfaces. Details/manual editor належать R12, не дублюй їхню роботу.

### Приймання

- Edit -> pick -> save -> back дає очікуваний план, швидкий back не губить write.
- Empty plan/active draft/conflicting edit states чесні й доступні.
- Усі cycle actions з inventory залишилися, дані не скинуто.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Permanent cycle edit і today-only R27 adjustment є різними intent/state. Не використовуй один settings setter для обох.
- Picker result застосовується через lifecycle-safe contract P13; success ack перед navigation, failure видимий. Зміна plan revision при активному draft дає conflict path, не втрату logs.
- Explicit historical date з Calendar зберігається при вході; Today start/resume окремий. Тести cross-tab + delayed save.

### Перевірка

check-quick; check-web-ui-guard; picker/annual targeted tests; connected cycle/picker flow.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R11 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R11.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

