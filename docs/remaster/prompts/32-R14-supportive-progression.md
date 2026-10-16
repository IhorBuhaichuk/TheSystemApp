# R14. Ненав'язливий прогрес і повернення після паузи

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен для UX acceptance; domain tests без телефона.**
**Залежності: R08, R12, P10, P11, R01.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R14**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab + the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/remaster/FEATURE_SCOPE.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/product-review/2026-10-08/03-FEATURE-VALUE.md](../../product-review/2026-10-08/03-FEATURE-VALUE.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodoBlock.kt`
- `app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/model/PlayerProgressionPolicy.kt`
- `domain/src/main/java/com/ihor/thesystem/domain/usecase/CompleteQuestUseCase.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Застосуй лише погоджені F29-F40/F52 спрощення: compact XP, real milestones, зрозумілі звички і supportive return path.
2. Спочатку presentation/copy; зміна streak/reward semantics потребує окремо зафіксованого domain contract і tests, не роби її приховано.
3. Немає punishment workout, платного streak rescue або читання як приросту сили. REST не failure.
4. Зберегти data/history/quest actions; duplicate display можна об'єднати тільки за inventory approval.

### Приймання

- Користувач може ігнорувати RPG й виконати весь core loop.
- Return after gap не вимагає overtraining і не скидає історію.
- Усі policy changes явні, tested; жодного storage deletion.

### Перевірка

check-quick; check-tests для policy/mapper changes; check-web-ui-guard; rest/missed/return journeys.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R14 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R14.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

