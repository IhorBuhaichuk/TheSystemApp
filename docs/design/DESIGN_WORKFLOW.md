# Figma -> нативний Android

Погоджений напрям, 2026-10-08. Початковий Status прийнятий D29/2026-10-14;
R04 done, решта екранів/core prototype ще не затверджені. Точний baseline у [FIGMA_MAP](FIGMA_MAP.md).

## Послідовність

1. R02: сценарії та повний action inventory наявного застосунку.
2. R03: file доступ + існуючі tokens/components у Figma Design, без зміни app.
3. R04: три редаговані концепти одного Status з однаковими даними й діями, але різними
   композиціями. Після відхилення v1 працюємо за [R04_REWORK_CONTRACT](R04_REWORK_CONTRACT.md):
   Action-first, Weekly rhythm, Daily agenda. Це альтернативи, не три теми для реалізації.
4. Власник вибирає напрям; записуємо node/revision та дозволені layout зміни.
5. R05: клікабельний core prototype. R06: решта вкладок, явне `new-approved`/`preserve-current` для secondary journeys та коротка перевірка core prototype з 3-5 реальними людьми без аніме-контексту. Результати й повторна перевірка blockers у NEW `docs/design/PROTOTYPE_FEEDBACK.md`; owner approval перед R07. Без учасників R06 `needs_owner`, доступні незалежні Figma-частини можна доробити.
6. R07: stateless Compose primitives/previews зі synthetic states. Не підміняти Figma HTML-макетом.
7. R08-R13: один екран за раз, чинні ViewModel events і native semantics.
8. P18/P19: реальна взаємодія, R15 motion, P20 профілювання; Figma prototype не доводить Android correctness.

Contracts R02: [UX_CONTRACT](UX_CONTRACT.md) (current/planned journeys, H01-H06) і
[ACTION_INVENTORY](ACTION_INVENTORY.md) (стабільні action IDs, callbacks/sources, відкриті G01-G06).
Читати лише потрібний journey/таблицю. H01-H06 є proposal; конкретне hierarchy/frame approval
фіксується після порівняння пропозицій R04 і до R05/реалізації; foundations R03 використовують чинні values.
Дозвіл переробити пропозиції не є схваленням остаточного frame. Static action wiring не є runtime proof.

## Джерела точності

- Розміри, spacing, кольори, typography, shape, shadow, іконки: Figma variables/components + Kotlin token map.
- Прив'язка node -> component -> Kotlin -> states -> screenshot живе у [FIGMA_MAP](FIGMA_MAP.md).
- Читати design context і screenshot конкретного node; не вгадувати параметри з довільного PNG.
- Figma px не є фізичними pixels пристрою: зіставляти з Compose dp/sp, density, fontScale і system insets.
- Різницю font rasterization/shadows перевіряти в Android і документувати; не обіцяти автоматичне pixel-perfect.
- Screenshot і концепт від imagegen є візуальним матеріалом, не editable layers і не джерелом constraints.
- Старий MINIMAL_HUD_V2_WORKLOG зберігає історичні approvals; він не задає новий remaster target. Для незмінених secondary screens достатньо baseline у FIGMA_MAP, не примусового нового макета.

## Примітиви й обмеження

Перевикористати SystemTheme, SystemTokens, SystemPanel, techSurface, SystemButton, існуючі dialogs/nav.
Зберегти спільне округлення великих панелей до іншого схваленого рішення. Читабельність важливіша за glow.
Не створювати паралельну дизайн-систему. Domain не залежить від Figma/Compose. Одна подія має той самий зміст
до і після UI migration. Точний список дозволених hide/merge дій має бути в action inventory, не в голові агента.

SharedTransition/AGSL/Lottie не prerequisites. Почати з існуючих Compose APIs; новий ефект лише коли
він передає стан і не погіршує first frame, logging або reduced motion. Не копіювати персонажів/лого аніме.

## Figma tools

Для мобільних концептів застосовувати локальний `the-system-figma-design`;
операційні скіли й межі перевірок записані в [FIGMA_SKILLS](FIGMA_SKILLS.md).

Figma authenticated 2026-10-08; поточний файл/nodes/pages у [FIGMA_MAP](FIGMA_MAP.md),
активний R04 entry у [Selected Design](R04_SELECTED_DESIGN.md). `whoami` не доводить write permission до будь-якого file.
Перед write/load design context завантажувати релевантний skill із Figma plugin/resource index:
`figma-create-new-file`, `figma-use`, `figma-generate-library`, `figma-generate-design`, `figma-design-to-code`.
Спочатку search_design_system/get_libraries, потім native design nodes. Для Android не використовувати web capture
або згенерований React-код. Невідома/недоступна можливість API -> позначити needs_owner, не імітувати результат.
Code Connect опційний після перевірки сумісності; звичайної FIGMA_MAP достатньо, SDK не оновлювати заради mapping.

Перевірені довідники: [Figma tools](https://developers.figma.com/docs/figma-mcp-server/tools-and-prompts/),
[структура файлу](https://developers.figma.com/docs/figma-mcp-server/structure-figma-file/),
[Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews).
