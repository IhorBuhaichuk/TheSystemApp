# R16. Free/Pro contracts без продажів

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний для domain/fake-provider tests.**
**Залежності: R01, P16.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R16**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-quality-review для фінального diff. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- [docs/playbooks/NEW_FEATURE.md](../../playbooks/NEW_FEATURE.md)

### Файли

- `domain/src/main/java/com/ihor/thesystem/domain/repository/`
- `domain/src/main/java/com/ihor/thesystem/domain/model/`
- `app/src/main/java/com/ihor/thesystem/core/di/RepositoryModule.kt`
- `domain/build.gradle.kts`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Вибери найменший domain FeatureAccess contract лише для погоджених capability IDs і typed reasons. Не вводь Android Billing types або speculative entitlement state machine.
2. NEW кандидати D/model/FeatureAccess.kt, D/usecase/GetFeatureAccessUseCase.kt та один provider contract лише за реальною потребою. Використай наявні boundaries; не створюй порожній framework і не торкайся Gradle без потреби.
3. Production provider завжди Free: весь існуючий core/safety/history/backup доступний offline. Fake provider лише в tests; не додавай debug-unlock UI або paywall. Для непідключених майбутніх capability не заявляй продажів/доступного Pro.
4. Перевір Free default, відсутність billing/network config, неможливість надати Pro з backup/UI flag, збереження core access. Не реалізовуй loading/verified/expired/grace/cancellation/cache TTL/clock/downgrade алгоритми: policy належить R19 після R17, реалізація R20/R21, повний lifecycle QA R23.
5. У FEATURE_MAP розрізни цей мінімальний implemented contract і projected billing ownership. Не став Billing SDK, не додавай cache/Room tables/service. Невирішений D14 не блокує цей Free-only крок.

### Приймання

- Core/safety/history/backup завжди доступні, UI не власник `isPro`.
- Unconfigured release працює як Free; backup не надає платні права.
- Мінімальні Free/default tests відтворювані; немає paid lifecycle machinery до рішення R19. Цей крок не затримує Free beta через невибраний provider/TTL.

### Перевірка

check-quick; :domain:test та relevant app tests; check-tests; graph boundary review.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R16 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R16.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

