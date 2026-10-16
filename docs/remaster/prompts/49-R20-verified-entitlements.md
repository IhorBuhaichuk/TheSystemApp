# R20. Перевірені права: service або managed provider

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний для service tests; sandbox access потрібний для integration.**
**Залежності: R19.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R20**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-quality-review. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/architecture/BILLING_ADR.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)

### Файли

- `docs/architecture/BILLING_ADR.md`
- `PRIVACY_POLICY.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Працюй тільки у NEW service/config/test paths, які визначив ADR. Якщо managed provider достатній, не будуй дубль verification backend.
2. Verified authoritative state, idempotent grant/revoke/ack responsibility, repeated/out-of-order events, refund, expired та identity mismatch tests.
3. Server secrets через захищений runtime secret store; жодних service keys у APK/repo/log. Purchase identifiers мінімізувати й redact.
4. Protected server resources перевіряють entitlement серверно; fake requests не grant. Підготуй deployment instructions/staging tests, deploy лише за explicit authorization.
5. Документуй provider outage/retry і reconciliation; онови privacy/data inventory.

### Приймання

- Unverified/pending/replayed request не створює Pro, repeated valid event не double grant.
- Revocation/expiry/account ownership коректні; може бути managed-provider evidence замість власного сервера.
- Секрети поза repo, staging proof окремо від unit fixtures.

### Перевірка

Service/provider tests за ADR, synthetic webhook fixtures, idempotency/security tests, no-secret checks. Без staging access needs_owner.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R20 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R20.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

