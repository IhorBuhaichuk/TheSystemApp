# R25. Підтримка, beta-фідбек і план інцидентів

**Модель: GPT-6 Luna; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний для runbook; потрібен для перевірки нового feedback entry, якщо погоджено.**
**Залежності: P26.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R25**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab + the-system-quality-review. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/implementation/evidence/P25.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/implementation/evidence/P26.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [PRIVACY_POLICY.md](../../../PRIVACY_POLICY.md)
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)

### Файли

- `README.md`
- `PRIVACY_POLICY.md`
- `docs/SCREENSHOTS_CHECKLIST.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Створи NEW docs/release/SUPPORT_RUNBOOK.md: consent-based feedback, contact owner, triage, data-loss/billing incident, backup assistance, pause rollout та corrective release.
2. Опиши мінімальні non-sensitive diagnostics/version/device info, opt-in export; не збирай health logs/purchase tokens і не додавай сторонню analytics автоматично.
3. Шаблон beta observation і weekly review: activation/корисна дія/return/dropoff, absolute counts, no fabricated cohort numbers.
4. Додай exact owner checklist для support/privacy URLs; app feedback entry лише окремий вузький UI change після approval.

### Приймання

- Є реальний support owner/channel або явний needs_owner; не вигадані email/URLs.
- Є план інциденту і відкату rollout без destructive downgrade Room schema.
- P27/R17 можуть використати компактні consented evidence, а не перечитувати переписки.

### Перевірка

Docs checks; якщо погоджено UI entry, check-quick + check-web-ui-guard і targeted device navigation.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R25 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R25.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

