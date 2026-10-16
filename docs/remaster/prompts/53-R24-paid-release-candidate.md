# R24. Paid RC: прозорість, безпека й право продавати

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен release variant на Play-compatible пристрої; owner Console/signing evidence.**
**Залежності: R23, P24, P27, R25.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R24**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-quality-review. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- NEW output prerequisite: `docs/architecture/BILLING_ADR.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [PRIVACY_POLICY.md](../../../PRIVACY_POLICY.md)
- [STORE_LISTING.md](../../../STORE_LISTING.md)

### Файли

- `PRIVACY_POLICY.md`
- `STORE_LISTING.md`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `docs/HEALTH_CONNECT_RATIONALE.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Склади candidate-specific GO/NO-GO для billing з R23 та повторними affected P24 checks; не повторюй весь аудит без diff reason.
2. Узгодь Data safety/privacy/processors/support/refund/manage links, actual products/price/trial і shipped Pro capability.
3. Перевір release artifact: no secrets/debug unlock/test prices, no medical guarantees, no unavailable-AI promises.
4. Перевір human gates: актуальні Play policy/Console eligibility, signing owner, expert/asset rights. Publication тільки explicit owner action.

### Приймання

- Зафіксовано hash/version/variant перевіреного кандидата і точні unresolved blockers.
- Paid availability не оголошена, поки R18-R23/owner gates не green.
- GO означає готовність кандидата, не автоматичний upload/production rollout.

### Перевірка

check-tests; check-web-ui-guard; lintRelease; bundleRelease/assembleRelease; release-device lifecycle smoke; policy evidence.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R24 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R24.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

