# R21. Android purchase adapter

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Play-enabled device/emulator + license tester для фінального gate.**
**Залежності: R20, R16.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R21**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: provider-specific skill лише якщо R19 установив. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/architecture/BILLING_ADR.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)

### Файли

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/ihor/thesystem/core/di/RepositoryModule.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Розшир мінімальний Free contract R16 за затвердженим ADR R19: domain paid-state policy та NEW adapter/cache paths. SDK тільки app/data, constructor injection; не припускай, що R16 вже реалізував lifecycle.
2. Products/offer details зі store; pending, cancellation, unavailable, reconnect, process recreation, restore і refresh typed UI state. Не trust client callback без verification R20.
3. У production відсутні debug unlocks. Не тримай purchase activity у ViewModel/repository і не блокуй Main startup.
4. Збережи Free behavior при disabled/unconfigured billing, не змінюй workout persistence. Update graph contracts.
5. Додай domain/adapter tests для unknown/loading/verified/expired/grace/hold, cancellation-period, fake clock/TTL/offline, downgrade і account conflict за R19. Не обирай новий TTL самостійно; повна end-to-end matrix лишається R23.

### Приймання

- Successful purchase відкриває лише verified capability; pending лишається pending.
- Refresh/relaunch не губить confirmed paid period, account mismatch не передає права іншому.
- Core loop продовжується offline і під час помилки store.

### Перевірка

check-quick; adapter fake-clock/unit tests; check-tests; release config scan; sandbox restore/purchase integration.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R21 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R21.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

