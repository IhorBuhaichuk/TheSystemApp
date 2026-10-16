# R12. Statistics: зрозумілий доказ прогресу

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Для charts/text acceptance device/emulator.**
**Залежності: R07, P12, P17.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R12**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/AnnualProgressionDetailsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/AnnualProgressionManualEditorScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Підключи approved Statistics до existing truthful data; графіки мають units/date range/sample amount/empty state.
2. Basic history і weekly summary Free, advanced placeholder не створювати. Не обчислюй progression у Canvas.
3. Beta diagnostics переміщуй/ховай тільки за approved R01/inventory; не видаляй recorder.
4. Sparse history/long labels/incomplete sets мають коректне пояснення, не вигадане покращення.
5. Annual details/manual editor виконати за disposition R06 у FIGMA_MAP: новий approved node або збереження поточного presentation із regression перевіркою shared tokens, save/edit/back. Annual plan presentation належить R11. Defer redesign не прибирає shipped історію/редагування або P14 data-safety gates.
6. Реалізуй approved [D27 progress destination](../../design/R04_SELECTED_DESIGN.md#progress-placement--d27): інформація колишнього Status-боксу «Деталі прогресу» доступна у Statistics за node/disposition R06. Використай справжні дані (у Figma fixture серія5 днів/рівень12/рангC), не hardcode і не дублюй business logic у Compose. Не повертай бокс у Status, не видаляй дані та не закривай доступ до власного прогресу підпискою.

### Приймання

- Історія користувача та edit paths збережені, статистика відповідає completed sets.
- Графік має доступне текстове summary і не покладається лише на колір.
- Відсутні платні заглушки або обіцянки ще не реалізованих insight.
- D27 information доступна у Statistics із коректним sparse/empty станом; перенесення не втрачає власну історію/розрахунки.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- D27 переносить progress details зі Status сюди, не повертає їх у workout box. Existing data збережені.
- P10/P12/P17 є єдиними джерелами partial/full/eligible counts; after finish/history edit/restore стан оновлюється без повторного відкриття.
- Тест одного partial session у report, Calendar, Statistics і local metrics має показувати узгоджені факти.

### Перевірка

check-quick; check-web-ui-guard; GetStatistics/progress proof tests; connected history/edit screenshots.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R12 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R12.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

