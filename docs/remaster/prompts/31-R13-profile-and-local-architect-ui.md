# R13. Profile, backup і корисний local Architect

**Модель: GPT-6.1 Sol; Reasoning: Medium.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен для permission/SAF/back flows.**
**Залежності: R07, P04, P05, P15, P16.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R13**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [SUBSCRIPTION_RULES.md](../../../SUBSCRIPTION_RULES.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/architect/ui/ArchitectScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/architect/ui/WorkoutAnalysisScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/architect/viewmodel/ArchitectUiState.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/BackupImportConfirmationDialog.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Реалізуй approved profile/settings/Architect states; backup/export/restore/privacy і permission refusal залишаються доступними.
2. Зрозуміло відрізни локальні дані від зовнішнього AI. No-AI має корисний brief/action, не dead premium shell.
3. Дотримуйся P04 preview/validation/confirm, не міняй storage contract заради layout.
4. Не додавати живий subscription CTA до готовності R22; відкриті design states не є продажами.
5. Для WorkoutAnalysis і backup/permission dialogs звір disposition R06 у FIGMA_MAP: лише approved redesign або `preserve-current` з regression перевіркою. Не вигадуй вторинний layout і не прибирай fallback/confirm/cancel дії.

### Приймання

- Profile edit/settings/SAF cancel/restore failure можна пройти і повернутись без втрати даних.
- HC denied/revoked/AI disabled не блокують core loop.
- Весь критичний текст і дії visible/reachable на UI matrix.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Налаштування обладнання/плану тут глобальні: validate/save/cancel і invalidation P09, активний draft перевіряється на конфлікт. A/EW session-only належить R09/R27, не викликає ці setters.
- Profile ShowError consumer доданий у DF09; не прибирати. Backup/restore має відновлювати нові P07/R27 поля за їх version/default contracts.
- Перевірити fail/cancel/return-to-Status без stale summaries і raw exceptions.

### Перевірка

check-quick; check-web-ui-guard; Architect/profile/backup targeted tests; focused device flows.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R13 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R13.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

