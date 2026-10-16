# R05. Прототип першої користі та тренування

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний; Figma prototype, за бажанням перегляд на телефоні.**
**Залежності: R04.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [x] Scope/prerequisites і потрібні approvals підтверджені.
- [x] D30 visual/content approval і control-to-data-flow audit записані; [перевірки](../evidence/2026-10-15-r05-data-flow.md).
- [x] Часткова Figma-реалізація2026-10-16:12 styled normal frames,13 shared action instances і8 secondary edges; [Present evidence](../evidence/2026-10-16-r05-prototype-checkpoint.md).
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.
Current implementation entry: [R05_CORE_PROTOTYPE](../../design/R05_CORE_PROTOTYPE.md).
Execution2026-10-16: [exact checkpoint](../evidence/2026-10-16-r05-prototype-checkpoint.md).
Twelve normal frames bound;22 still pending after MCP quota. Reuse the saved node ledger.
Current2026-10-15/D30: v2 visual/content accepted; owner authorizes data-flow audit and
prompt reconciliation. Previous pause is superseded. Read
[R05_DATA_FLOW](../../design/R05_DATA_FLOW.md). Remaining gates: secondary/360 links,
shared bindings and consistent fixtures, not another visual-direction vote.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R05**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/design/UX_CONTRACT.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- [Accepted initial Status / D29](../../design/R04_SELECTED_DESIGN.md#final-acceptance--d29): використай поточний B після D26-D28, не повертай вибір A/C чи старих назв/artwork. Це baseline для нових станів/переходів, не вже готовий clickable core flow.
- [R04 B interaction checklist](../../design/R04_B_REFINEMENT_CHECKLIST.md), B17/STA-08: рядок справи утримується приблизно1-1.5с для reorder, без окремої ручки.

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/onboarding/ui/OnboardingScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/workout/ActiveDayCard.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/components/dialogs/WorkoutReportDialog.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Побудуй clickable onboarding -> first Today -> workout -> logging -> report з real Ukrainian labels, Auto Layout і shared instances.
2. Покрий training/rest/recovery/unknown/no plan; incomplete/draft-restored/saving/committed/local fallback. Ці стани узгоджуй з поточними contracts і planned P07-P10, а не власними business rules.
3. Передбач keyboard, довгі назви, validation message, короткий шлях до complete/edit/finish без auto-completing попередніх значень.
4. Запиши approval і всі core node links у FIGMA_MAP. Figma testing не називай виконанням Room або реального Today.
5. Передай в R06 стабільний prototype revision і короткі нейтральні завдання для ранньої UX-перевірки: знайти наступну дію та пояснення, записати підхід, завершити тренування, зрозуміти звіт. R05 підтверджує працездатність прототипу; перевірку зі справжніми людьми та її evidence виконує R06.
6. Заверши v2 secondary/360 routes A/EW/D/SE/SB/RD, back/cancel/partial confirmation. Align preview/Status fixture values without changing D29 layout. Map each action to R05_DATA_FLOW and its implementation owner; static Figma is not persistence.
7. Зафіксуй todo hold/drag/cancel states без видимого handle та зі збереженими checkbox/menu actions. Якщо Figma не відтворює вільне row-wide перетягування точно, покажи state transition і чесно запиши обмеження; реальний gesture/scroll/conflict тест належить R08, не оголошуй його виконаним у прототипі.

### Приймання

- Можна пройти основний сценарій і повернутись назад без dead end.
- Відмінність saved і AI analysis очевидна; no-AI не виглядає як поломка.
- 360x640/normal frames з критичним текстом без обрізання; owner approval прототипу.

### Перевірка

Пройти prototype links, перевірити state variants і screenshots; docs-only.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R05 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R05.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

