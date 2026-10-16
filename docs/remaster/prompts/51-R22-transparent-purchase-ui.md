# R22. Прозорий paywall і керування підпискою

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен Play sandbox і UI matrix.**
**Залежності: R21, R18, R13.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R22**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [MONETIZATION_STRATEGY.md](../../../MONETIZATION_STRATEGY.md)
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)
- NEW output prerequisite: `docs/architecture/BILLING_ADR.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt`
- `app/src/main/java/com/ihor/thesystem/core/navigation/Routes.kt`
- `app/src/main/java/com/ihor/thesystem/core/navigation/AppNavGraph.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Спочатку editable Figma paywall/manage states і owner approval, потім NEW app/feature/subscription Compose/VM paths з ADR.
2. Однакові права monthly/yearly; повна charge/period, eligibility/trial/renewal details зі store, clear Free exit, restore/manage.
3. Контекстний entry тільки з робочої R18 capability; не interrupt onboarding/logging/finish/backup.
4. Loading/pending/already subscribed/error/expired і no offers мають коректні дії, без raw exception/fake urgency.

### Приймання

- Price/offer не hardcoded, missing offers не дають оманливу кнопку оплати.
- Користувач може відмовитися й завершити Free workflow; navigate back не губить draft.
- TalkBack/font1.3/2.0 критичний billing copy читається, legal links реальні.

### Перевірка

check-quick; check-web-ui-guard; mapper/state tests; connected sandbox purchase/cancel/manage/free-exit; screenshots.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R22 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R22.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

