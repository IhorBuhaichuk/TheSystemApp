# R08. Onboarding і Status: ясна перша дія

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Потрібен device/emulator для first-plan/scroll/navigation перевірки.**
**Залежності: R07, P07, P08, P09, P10, R27.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R08**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- [R04 B interaction checklist](../../design/R04_B_REFINEMENT_CHECKLIST.md), тільки B17/STA-08 та остаточно схвалені деталі Status.
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/playbooks/UI_POLISH.md](../../playbooks/UI_POLISH.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/feature/onboarding/ui/OnboardingScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/StatusScreen.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgStatusDashboard.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodayOrderBlock.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/ui/RpgTodoBlock.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/TodayOrderUiMapper.kt`
- `app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusUiState.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. Підключи approved first-plan/Status nodes до existing UI state/events. Domain правила з P07-P10 не дублювати.
2. Today показує дію, коротке why, одну primary CTA і результат/наслідок. Unknown/rest/completed не маскувати training state.
3. Збережи todos/quest/profile та всі actions із inventory; structural change тільки approved. Вертикальний scroll не перемикає mode.
4. Не вимагай permission/Pro для першої користі; після onboarding реальний Status без route flash.
5. Owner-approved reorder: без значка з трьох рисок. Утримання самої справи в будь-якому місці приблизно1-1.5с, потім drag вище/нижче. Збережи onTodosReordered, реальні IDs і cancellation; звичайні короткі taps checkbox/menu та вертикальна прокрутка не повинні запускати reorder. Доступні semantic move-up/down є альтернативою, не заміною основного жесту. Точний threshold перевір на device, не оголошуй його готовим за Figma.
6. Дотримуйся approved [D27](../../design/R04_SELECTED_DESIGN.md#progress-placement--d27): на Status немає окремого боксу «Деталі прогресу». Його інформація належить Statistics/R12; не видаляй storage/state/обчислення або інші потрібні дії. Після останньої справи лише узгоджений section gap і мінімальний inset/nav clearance, не старий резерв під видалений блок. Не перенось блок у workout summary. Поточний запит не закриває R12 автоматично.
7. Approved [D28](../../design/R04_SELECTED_DESIGN.md#weekly-counter--d28): не показуй окремий текст «1 / 3 виконаних тренувань»/його динамічний еквівалент на Status. Дати/позначки тренувань у тижні лишаються; underlying weekly aggregation/state/history не видаляти. Без нового лічильника-заміни.

### Приймання

- Кожен основний стан зіставлено з existing mapper, є targeted mapping regression при зміні.
- CTA reachable 360x640/1.3/IME, горизонтальний mode лише за явного horizontal intent.
- Реальний onboarding -> Today -> workout navigation пройдено, не тільки preview.
- Todo gesture regression: row-wide hold/drag змінює тільки порядок, cancel не змінює його; короткі checkbox/menu taps і звичайний scroll працюють; persistence зберігає identity. Перевір також accessibility reorder без вимоги точного drag.
- Немає Status progress card/зайвої порожнечі в кінці scroll; остання справа повністю доступна над navigation/insets, дані прогресу не видалені.

### Узгодження R05 / D30

Прочитай [R05_DATA_FLOW](../../design/R05_DATA_FLOW.md): лише relevant control rows,
source aliases і findings. Нижче обов'язкові доповнення до acceptance, не виконані зміни.

- Прив'яжи G/H/S/E/B/P/SB до реальних P07 state/events, без старого обов'язкового NAME -> cycle-length сценарію. Back/validation/preview/save відображають його contracts; статичні Figma відповіді не стають hardcoded даними.
- Status ViewPlan і OpenOrResumeWorkout використовують різні typed actions P08; summary отримує effective plan R27. D29 layout і D25-D28 збережені.
- Todo add є parameterless standalone intent (DF08 repaired); не повертай count-as-questId. Menu: wire rename/edit і remove/cancel через ViewModel/repository, зберігаючи IDs/children; не малюй дію без handler.
- Перевір ShowError consumers; критичні довгі operations мають state, не лише Toast. Тести зміни input -> save -> інша вкладка, cancellation, retry і process restore.

### Перевірка

check-quick; check-web-ui-guard; TodayOrderUiMapper/onboarding tests; focused connected journey + screenshots.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R08 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R08.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

