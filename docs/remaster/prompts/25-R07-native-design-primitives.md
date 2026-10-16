# R07. Нативні primitives за затвердженим дизайном

**Модель: GPT-6.1 Sol; Reasoning: High.** Вибір задає власник у Codex; [policy](../MODEL_ROUTING.md).
**Телефон: Для остаточної visual перевірки device/emulator; compile/previews без телефона.**
**Залежності: R06.** Також перевір [QUEUE](../QUEUE.md): там додаткові gates для P-пакета.

## Прогрес

- [ ] Scope/prerequisites і потрібні approvals підтверджені.
- [ ] Завдання реалізовано в межах цього prompt.
- [ ] Host-перевірки пройдені; результати записані.
- [ ] Runtime/owner/value gates цього prompt пройдені або явно не застосовуються з обґрунтуванням.
- [ ] Evidence, карта й статус оновлені; крок завершено.

Порожні checkbox означають невиконане, не недолік документа. Галочку ставити лише з evidence.

## Промпт

Працюй у `C:/Users/gesha/AndroidStudioProjects/TheSystem-master`. Виконай тільки **R07**.
Спочатку GRAPH_REPORT.md, AGENTS.md, [EXECUTION_RULES](../EXECUTION_RULES.md), свій рядок [PROGRESS](../PROGRESS.md).
Skill: the-system-native-ux; edge-to-edge лише за inset problem. Не завантажуй усі skills чи весь аудит. Прочитай один relevant FEATURE_MAP row.
Нижче точковий контекст, не команда перечитати весь репозиторій:
- [docs/design/DESIGN_WORKFLOW.md](../../design/DESIGN_WORKFLOW.md)
- [docs/design/FIGMA_MAP.md](../../design/FIGMA_MAP.md)
- [docs/playbooks/UI_POLISH.md](../../playbooks/UI_POLISH.md)

### Файли

- `app/src/main/java/com/ihor/thesystem/core/theme/SystemTokens.kt`
- `app/src/main/java/com/ihor/thesystem/core/theme/Theme.kt`
- `app/src/main/java/com/ihor/thesystem/core/theme/Dimensions.kt`
- `app/src/main/java/com/ihor/thesystem/core/theme/Type.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemPanels.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemGlassComponents.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemBottomNavBar.kt`
- `app/src/main/java/com/ihor/thesystem/core/ui/components/SystemDialogComponents.kt`

Це entry points, не дозвіл переписати їх усі. Якщо контракт переміщений, знайди symbol через карту/rg.
NEW outputs створюй лише за потребою; future file в prerequisite має існувати після його виконання.

### Завдання

До змін коду прочитай NEW prerequisite `docs/design/PROTOTYPE_FEEDBACK.md` із R06:
потрібні реальні спостереження, закриті core blockers і owner approval переходу.
Без цього gate R07 не починати; не генеруй research evidence самостійно.

1. Зчитай context і screenshot approved foundation nodes через mandatory Figma skill. Зістав dp/sp, не фізичні pixels.
2. Еволюційно онови existing tokens/primitives; жодної паралельної DesignSystemV2. Stateless states і callback contracts лишаються сумісними.
3. Додай previews/synthetic states для button/field/panel/dialog/nav; content wrapping, focus, disabled, error без нової domain logic.
4. Зміни shared effects перевір на всіх existing consumers до rollout. Runtime layout/action зміни за межами primitive contract не робити.
5. Закрий technical handoff із [D29](../../design/R04_SELECTED_DESIGN.md#final-acceptance--d29): звір raw decorative colors із semantic tokens, shared gradient/shadow bindings та Figma-vs-Android renderer limits. Owner approval appearance не є перевіреним contrast/fontScale/inset результатом; не підміняй ним UI matrix і P19. Не додавай невибраний header artwork.

### Приймання

- Одне джерело shape/color/material/motion, без повтореного inline style.
- Критичні labels/48dp touch semantics/insets доступні на UI matrix; existing actions збережені.
- FIGMA_MAP містить actual token/component mapping; немає forced SDK upgrade.

### Перевірка

check-quick; check-web-ui-guard; focused component tests, UI matrix; milestone check-tests.
Назви check-quick/check-tests/check-room/check-web-ui-guard означають `scripts/<name>.cmd`.
Device safety та UI matrix визначені в EXECUTION_RULES; compile не дорівнює виконаним device tests.

Наприкінці онови тільки R07 у PROGRESS, checkbox цього файлу і `docs/remaster/evidence/R07.md`:
що зроблено, точні checks/results, device/owner gaps, next ID. Дані checklist узгодь із evidence,
не став done за часткову перевірку. Онови relevant MD при зміні contracts/рішень за DOC_MAINTENANCE.
Не commit/push/merge/deploy, не запускай наступний prompt автоматично.

