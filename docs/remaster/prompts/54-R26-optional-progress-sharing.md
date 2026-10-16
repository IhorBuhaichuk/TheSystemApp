# R26. Опційний експеримент поширення прогресу

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен для Android Sharesheet/privacy preview.**
**Залежності: P27, P12, R12.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R26**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-product-lab + the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/implementation/evidence/P27.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/product-review/2026-10-08/04-POSITIONING-AND-GROWTH.md](../../product-review/2026-10-08/04-POSITIONING-AND-GROWTH.md)
- NEW output prerequisite: `docs/remaster/FEATURE_SCOPE.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/statistics/ui/StatisticsScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt`
- `app/src/main/AndroidManifest.xml`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Спочатку підтверди конкретний запит beta. Немає користі/evidence -> deferred, не будуй social network.
2. Запропонуй один opt-in share card із реальним completed progress, default без імені/ваги/health details. Preview/redaction/cancel до Sharesheet.
3. Реалізація native Compose/Android, FileProvider з мінімальним scoped cache access; без contacts/referral coercion/upload.
4. Окремий approved Figma node і локальні tests. Не називай фічу вірусною до спостереження реального добровільного поширення.

### Приймання

- Нічого не публікується автоматично; share cancel не змінює workout/rewards.
- Only opt-in поля, truthful data, no permanent public export path.
- Після невеликого експерименту рішення keep/change/remove з критерієм, не permanent scope creep.

### Перевірка

check-quick; check-web-ui-guard; aggregation/privacy tests; Sharesheet/manual cancellation + content URI access tests.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R26 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R26.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

