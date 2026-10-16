# R23. Перевірка повного циклу підписки

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Обов'язково Play-enabled device/emulator і license tester; жодних реальних списань.**
**Залежності: R22.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R23**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-quality-review. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- NEW output prerequisite: `docs/architecture/BILLING_ADR.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/remaster/evidence/R20.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/remaster/evidence/R21.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/remaster/evidence/R22.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt`
- `app/build.gradle.kts`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Додай adapter/service/UI tests у точних paths ADR: pending, verified, canceled-renewal, grace, hold, expiry, refund/revoke, restore, account switch.
2. Зістав sandbox transition evidence і deterministic fixtures: те, що sandbox не відтворює надійно, познач unverified, не fake pass.
3. Перевір offline TTL/clock skew і повторні/out-of-order events, process death під purchase і active workout при expiry.
4. Простежи Free data/export/backup/finish після downgrade, release без fake entitlement. Виявлені bugs виправляй вузько з regression test.

### Приймання

- Вся lifecycle matrix має expected/observed/test evidence, critical unknown не закритий.
- Немає втрати workout data, double entitlement чи raw errors.
- Незалежний review попросити окремо; не заявляти його, якщо той самий автор лише перечитав код.

### Перевірка

check-tests; release lint/build; connected sandbox tests; service verification suite; relevant P18 rerun.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R23 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R23.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

