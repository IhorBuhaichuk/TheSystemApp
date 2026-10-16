# R04. Три напрями й вибір дизайну

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Не потрібний; Figma.**
**Залежності: R03.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [x] Scope/prerequisites і потрібні approvals підтверджені.
- [x] Завдання реалізовано в межах цього prompt.
- [x] Host-перевірки пройдені; результати записані.
- [x] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [x] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

R04-v1 відхилено власником (1/10); історичні screenshots не є acceptance.
Перероблення: спочатку [R04_REWORK_CONTRACT](../../design/R04_REWORK_CONTRACT.md).
Історичний D24 затверджував лише B placement. D29/2026-10-14 явно затверджує поточний
початковий Status після D26-D28; R04 done, не production UI чи core prototype.
Поточний scope/варіанти/жест утримання: [R04_B_REFINEMENT_CHECKLIST](../../design/R04_B_REFINEMENT_CHECKLIST.md).
Latest entry: [Accepted design / D29](../../design/R04_SELECTED_DESIGN.md#final-acceptance--d29), не перечитувати весь пакет.
Погоджена назва третьої вкладки «Тренування»/D25; internal Cycle та candidate IDs незмінні.
Виконані частини round2:
- [x] 32 icon/3 week replacements;18 icons/W01/W05 retain native IDs; exports inspected.
- [x] Open workout below View plan; nav alone Fixed; both actual Present scroll checks passed.
- [x] Later narrow EX revision3: all5 exercise candidates replaced, other45 IDs/names preserved; actual export inspected.
- [x] D26:10 chosen icons/W02 applied to both phones,75 only beside XP icon; native PNG/Present inspected.
- [x] Full-content canvas overview, separate phone/library pages;6 obsolete roots removed, needed masters retained.
- [x] D27: progress box removed from all3 Status frames; excess lower space closed, PNG/phone scroll ends verified; Statistics destination recorded for R06/R12.
- [x] D28: redundant weekly counter removed from all3 frames,7 W02 dates/states retained; current native PNG and both Present tops inspected.
- [x] D29: current titles/copy/material appearance/header accepted as shown; no unchosen art inserted, its alpha-mask check N/A. Native material/binding/contrast verification remains R07/R08/P19, not claimed passed here.
За новим запитом A/C видалені після збереження shared masters; історичні три-напрямні
вимоги нижче не означають повторно створювати A/C чи знову питати про композицію.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R04**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- NEW output prerequisite: `docs/design/UX_CONTRACT.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- NEW output prerequisite: `docs/design/ACTION_INVENTORY.md`. Прочитай після завершення його owner task; не вигадуй відсутній документ.
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)

### Файли

- `docs/design/FIGMA_MAP.md`
- `docs/remaster/DECISIONS.md`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

1. У Figma створи 3 справді різні editable Status композиції з однаковими synthetic facts/actions: дія першою, ритм тижня, щоденний список. Material expression може відрізнятися, але не підміняти UX різницею шрифтів.
2. Не підміняй вибір приховуванням потрібних даних. Контрольований cyan/violet, великі rounded panels, читабельна українська типографіка, не fandom-реквізит.
3. Покажи small/normal frames і коротко поясни trade-offs. Imagegen лише optional visual references, не точний layout.
4. Запитай конкретний вибір власника; зафіксуй approved node/revision/дозволені structural зміни. Не впроваджуй 3 themes і не починай production UI до вибору.

### Приймання

- 3 зіставні редаговані варіанти з різною композицією/CTA placement, primary task читається без аніме-знань; corrections і event parity з R04_REWORK_CONTRACT перевірені.
- Action inventory не втрачений; owner decision явне, не inferred from silence.
- Обраний лише один напрям. Latest owner override: A/C прибрати; зберегти shared masters
  і історичну evidence, не дублювати відхилені екрани в активному макеті.

### Перевірка

Figma screenshots/node inspection; docs-only. needs_owner, якщо вибір ще не зроблений.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R04 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R04.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

