# R15. Рух, який допомагає, а не заважає

**Модель: GPT-6 Luna; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Фізичний телефон бажаний для плавності; screenshots/preview не доводять frame time.**
**Залежності: P18, P19, R07.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R15**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- [docs/design/DESIGN_WORKFLOW.md](../../design/DESIGN_WORKFLOW.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/core/theme/SystemTokens.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemGlassComponents.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutReportDialog.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Додай лише approved state transitions/press feedback/progress через shared motion tokens та існуючі Compose APIs.
2. Поважай disabled/reduced animation, зупиняй decorative infinite work поза видимістю/lifecycle.
3. Жодної затримки complete/save/finish, blocking celebration, AGSL/Lottie dependency без виміру потреби.
4. Збережи baseline before/after для P20; важкий ефект видалити/спростити, не маскувати jank.

### Приймання

- State change зрозумілий без animation; actions не чекають на неї.
- Немає нового постійного rendering work на idle/background.
- Performance claim тільки з P20 measurement, цей prompt не видає fake FPS.

### Перевірка

check-quick; check-web-ui-guard; animation-disabled UI tests; actual motion inspection, regression P18 affected flows.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R15 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R15.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

