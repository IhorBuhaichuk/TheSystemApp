# R01. Зафіксувати межі продукту

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний.**
**Залежності: немає.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [x] Scope/prerequisites і потрібні approvals підтверджені.
- [x] Завдання реалізовано в межах цього prompt.
- [x] Host-перевірки пройдені; результати записані.
- [x] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [x] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

Стан 2026-10-12: документи й host checks готові; весь пакет [SC-R01-v1 A-D](../FEATURE_SCOPE.md)
погоджений власником. Runtime/value research для документаційного R01 не застосовуються;
їхні майбутні gates залишаються у своїх tasks. Статус `done`; докази у [R01](../evidence/R01.md).

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R01**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/product-review/2026-10-08/README.md](../../product-review/2026-10-08/README.md)
- [docs/product-review/2026-10-08/03-FEATURE-VALUE.md](../../product-review/2026-10-08/03-FEATURE-VALUE.md)
- [docs/product-review/2026-10-08/05-RECOMMENDATIONS.md](../../product-review/2026-10-08/05-RECOMMENDATIONS.md)
- [docs/remaster/DECISIONS.md](../DECISIONS.md)
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)

### Файли

- `PRODUCT_STRATEGY.md`
- `MVP_DEFINITION.md`
- `docs/remaster/DECISIONS.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Узгодь поточні strategy/MVP з broad-audience personal system; тренування головне реалізоване ядро, звички допоміжні. Збережи no-AI/local-first.
2. Для всіх F01-F53 зроби компактне рішення keep/improve/defer/consider-hide з причиною. Винеси у NEW docs/remaster/FEATURE_SCOPE.md; не дублюй весь аудит.
3. Розділи погоджений напрям і конкретні відкриті рішення щодо F23/F35-F39/F43/F45-F46/F51/F52. Запропонуй найменший MVP; не видаляй Kotlin/дані.
4. Зафіксуй Free baseline і Pro лише як кандидати. Price/provider не вигадуй. Отримай owner approval для конкретного hide/merge scope, незатверджене познач open.

### Приймання

- Кожен F-ID має рішення або явне open, жодна safety/data функція не обрізана.
- PRODUCT_STRATEGY/MVP не суперечать поточній аудиторії; no-AI Free залишається повним.
- R01 done тільки після зафіксованого схвалення конкретного scope; до цього needs_owner.

### Перевірка

Docs-only checks за EXECUTION_RULES; Kotlin/KTS hashes незмінні.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R01 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R01.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

