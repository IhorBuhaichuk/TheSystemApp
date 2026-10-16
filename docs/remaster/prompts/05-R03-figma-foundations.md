# R03. Figma: файл, tokens і базові компоненти

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний; потрібен доступ до Figma.**
**Залежності: R02.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [x] Scope/prerequisites і потрібні approvals підтверджені.
- [x] Завдання реалізовано в межах цього prompt.
- [x] Host-перевірки пройдені; результати записані.
- [x] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [x] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R03**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux + Figma skills за DESIGN_WORKFLOW. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/DESIGN_WORKFLOW.md](../../design/DESIGN_WORKFLOW.md)
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/UX_CONTRACT.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/src/main/java/com/ihor/thesystem/core/theme/SystemTokens.kt`
- `app/src/main/java/com/ihor/thesystem/core/theme/Dimensions.kt`
- `app/src/main/java/com/ihor/thesystem/core/theme/Type.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemGlassComponents.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Read-only перевір доступ. Використай погоджений file; якщо немає, створи окремий THE SYSTEM Remaster через Figma після mandatory create-file/use skills. Не змінюй чужі nodes.
2. Search existing design system/libraries; перенеси потрібні наявні tokens у variables/styles і 5-8 основних компонентів, не весь catalog одразу.
3. Прив'яжи великі округлені panels, buttons, fields, labels/states до спільних values. Не створюй web UI або screenshot-as-layout.
4. Запиши реальні file/node URLs, names/variants/token values -> Kotlin symbols у FIGMA_MAP. Не встановлюй Code Connect/SDK upgrade без потреби.

### Приймання

- Є editable native nodes і reusable instances; текст/іконки не запечені в PNG.
- Зміна одного token послідовно змінює його instances, підтверджено inspection/screenshot.
- Файл доступний власнику; quota/permission gap позначений, не замаскований.

### Перевірка

Figma inspection + screenshots + local links; docs-only source comparison. Жодних app builds.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R03 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R03.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

