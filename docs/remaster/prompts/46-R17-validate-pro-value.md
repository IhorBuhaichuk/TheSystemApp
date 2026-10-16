# R17. Перевірити Pro-цінність і цінову гіпотезу

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Для інтерв'ю необов'язковий; потрібні справжні beta users/feedback.**
**Залежності: P27, R16.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R17**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- NEW output prerequisite: `docs/remaster/FEATURE_SCOPE.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/implementation/evidence/P27.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `MONETIZATION_STRATEGY.md`
- `SUBSCRIPTION_RULES.md`
- `docs/remaster/DECISIONS.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Використай реальні beta висновки, не синтетичні відгуки. Порівняй recurring problem із наявною Free користю.
2. Вибери максимум одну додаткову Pro capability і сформулюй observable outcome + non-goals + acceptance у NEW docs/remaster/PRO_CAPABILITY.md.
3. Зроби простий price/cost scenario з актуальними primary sources, локальні ціни/комісії як assumptions; не обіцяй прибуток.
4. Покажи власнику go/defer для subscription, monthly/yearly/trial hypotheses, обсяг human research. Без evidence needs_beta_data; без затвердження needs_owner.

### Приймання

- Є evidence конкретної повторюваної цінності, а не бажання додати paywall.
- Free baseline не урізаний, ціна/trial/provider не видаються фактом до approval.
- Можливий чесний результат deferred; це не блокує Free release.

### Перевірка

Docs-only; source/date checks, відсутність sensitive beta data; owner decision recorded.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R17 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R17.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

