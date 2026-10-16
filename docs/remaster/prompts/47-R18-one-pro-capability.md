# R18. Одна реальна Pro-можливість

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен для connected UI acceptance.**
**Залежності: R17, P12, R12.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R18**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab для scope; native-ux для UI. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/remaster/PRO_CAPABILITY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)

### Файли

- `domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Default candidate: пояснене порівняння завершених тренувальних періодів понад безкоштовний weekly summary. Реалізуй тільки якщо це саме затвердив R17.
2. Якщо затверджено іншу capability, спочатку заміни цей file scope/acceptance у PRO_CAPABILITY і prompt; не реалізуй навмання.
3. Domain aggregation + bounded queries + UI mapper, достатність даних/period changes/incomplete sets. Локальне виконання без remote AI.
4. Перед UI додай approved Figma node. Використай R16 capability contract; fake Pro тільки tests. Не закривай Free history/basic charts.

### Приймання

- Корисний результат відтворюється на synthetic long/sparse histories, має пояснені дані/межі.
- Free regressions відсутні, expiry не знищує результати/історію.
- Немає claim 'персональний тренер/медична оцінка' без відповідної підстави.

### Перевірка

check-quick; aggregation/mapper tests; check-tests; check-room + SQL tests якщо query/schema зміни; UI/free-pro tests.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R18 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R18.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

