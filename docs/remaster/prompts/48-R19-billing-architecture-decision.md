# R19. Вибрати billing provider і межі backend

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний для ADR; потрібні owner/Console відомості без секретів.**
**Залежності: R17, R16.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R19**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-quality-review. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- NEW output prerequisite: `docs/remaster/PRO_CAPABILITY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `PRIVACY_POLICY.md`
- `SUBSCRIPTION_RULES.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. За current official docs порівняй direct Play Billing + verification service і managed provider; витрати, SDK/privacy, offline, portability/support.
2. Створи NEW docs/architecture/BILLING_ADR.md: provider/версія-сумісність, identity/restore/account conflict, verification authority, cacheTTL, webhook/RTDN, operational owner.
3. Визнач мінімальні конкретні paths для adapter/service/tests і deployment topology. Не змінюй Kotlin version/build stack без доказу.
4. Попроси owner затвердити provider, витрати, data processors і identity. Потрібний skill вибирай лише релевантний provider, pin source/license, не встановлюй величезний bundle.
5. Не створюй paid account/products і не проси секрети в чат; за відсутності доступу оформи owner checklist.
6. Тут, а не в R16, затвердь D14: unknown/loading, verified/expired/grace/hold, cancellation до кінця періоду, bounded offline cache/TTL, clock changes, downgrade і restore/account conflict. Зафіксуй очікувані переходи й test cases для R20/R21/R23; розширюй мінімальний Free contract лише під approved provider та Pro capability.

### Приймання

- Є затверджений ADR з точними файлами й failure policy, не 'додамо backend потім'.
- Локальний core працює без логіну/мережі; minimal identity пояснена.
- R20/R21 runnable лише після approval, ніякої production оплати.

### Перевірка

Docs-only; перевірити current Billing compatibility/policy primary sources; secrets scan docs.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R19 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R19.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

